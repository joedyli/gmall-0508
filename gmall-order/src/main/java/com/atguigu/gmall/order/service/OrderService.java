package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.interceptor.UserInfo;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.OrderSubmitResponseVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallWmsFeign gmallWmsFeign;
    @Autowired
    private GmallUmsFeign gmallUmsFeign;
    @Autowired
    private GmallSmsFeign gmallSmsFeign;
    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private GmallOmsFeign gmallOmsFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private JedisPool jedisPool;

    private static final String TOKEN_PREFIX = "order:token:";

    public OrderConfirmVO confirm(Map<Long, Integer> map) {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        // 验证登录状态，获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 生成orderToken
        String orderToken = IdWorker.getTimeId();
        orderConfirmVO.setOrderToken(orderToken);
        // 保存orderToken唯一标志到redis，防止重复提交
        this.redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);

        // 收获地址列表
        CompletableFuture addressesFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> listResp = this.gmallUmsFeign.queryAddressByUserid(userInfo.getUserId());
            orderConfirmVO.setAddressas(listResp.getData());
        }, threadPoolExecutor);

        // 订单详情信息
        List<OrderItemVO> orderItemVOS = new ArrayList<>();
        // 遍历map获取订单中清单
        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                OrderItemVO orderItemVO = new OrderItemVO();
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.skuInfo(entry.getKey());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                orderItemVO.setCount(entry.getValue());
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                orderItemVO.setPrice(skuInfoEntity.getPrice());
                orderItemVO.setSkuId(skuInfoEntity.getSkuId());
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                Resp<List<SkuSaleAttrValueEntity>> saleValueResp = this.gmallPmsFeign.querySaleAttrValuesBySkuId(entry.getKey());
                orderItemVO.setSkuAttrValue(saleValueResp.getData());
                orderItemVOS.add(orderItemVO);
            }
            orderConfirmVO.setItems(orderItemVOS);
        }, threadPoolExecutor);


        // 积分信息
        CompletableFuture boundsFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> entityResp = this.gmallUmsFeign.info(userInfo.getUserId());
            MemberEntity memberEntity = entityResp.getData();
            orderConfirmVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        CompletableFuture.allOf(itemsFuture, addressesFuture, boundsFuture).join();

        return orderConfirmVO;
    }

    public OrderSubmitResponseVO submit(OrderSubmitVO orderSubmitVO) {
        OrderSubmitResponseVO responseVO = new OrderSubmitResponseVO();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String orderToken = orderSubmitVO.getOrderToken();
        responseVO.setOrderToken(orderToken);

        // 1. 防止重复提交
        Jedis jedis = null;
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            jedis = jedisPool.getResource();
            Long flag = (Long) jedis.eval(script, Arrays.asList(TOKEN_PREFIX + orderToken), Arrays.asList(orderToken));
//        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
            if (flag != 1) {
                responseVO.setCode(1);
                responseVO.setMsg("请不要重复提交，或者页面已过期");
                return responseVO;
            }
        } finally {
            if (jedis != null)
                jedis.close();
        }

        // 2. 实时校验价格，从数据库获取实时价格
        BigDecimal currentTotalPrice = new BigDecimal(0);
        List<OrderItemVO> items = orderSubmitVO.getItems();// 获取送货清单
        for (OrderItemVO item : items) {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.skuInfo(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            currentTotalPrice = currentTotalPrice.add(skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount())));
        }
        if (currentTotalPrice.compareTo(orderSubmitVO.getTotalPrice()) != 0) {
            responseVO.setCode(2);
            responseVO.setMsg("页面已过期，请刷新重试！");
            return responseVO;
        }

        // 3. 校验库存，并锁定库存
        List<SkuLockVO> skuLockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO lockVO = new SkuLockVO();
            lockVO.setSkuId(orderItemVO.getSkuId());
            lockVO.setNum(orderItemVO.getCount());
            lockVO.setOrderToken(orderToken);
            return lockVO;
        }).collect(Collectors.toList());
        Resp<Object> objectResp = this.gmallWmsFeign.checkAndLockStock(skuLockVOS);
        Object data = objectResp.getData();
        if (data != null) {
            responseVO.setCode(3);
            responseVO.setMsg(data.toString());
            return responseVO;
        }

        // 4. 创建订单，创建成功后，订单的状态是未付款状态
        Resp<OrderEntity> orderEntityResp = this.gmallOmsFeign.saveOrder(orderSubmitVO, userInfo.getUserId());
        OrderEntity orderEntity = orderEntityResp.getData();
        if (orderEntity == null) {
            // 解锁库存  TODO： 发送消息给库存系统，解锁库存

            responseVO.setCode(4);
            responseVO.setMsg(data.toString());
            return responseVO;
        }

        // 5. 删除购物车中的记录，发送消息给购物车，删除购物车
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        map.put("skuIds", skuIds);
        this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE", "cart.delete", map);

        responseVO.setCode(0);
        responseVO.setMsg("订单创建成功");
        responseVO.setOrderEntity(orderEntity);
        return responseVO;
    }

    public void paySuccess(String out_trade_no) {

        this.amqpTemplate.convertAndSend("ORDER-EXCHANGE", "order.pay", out_trade_no);
    }
}
