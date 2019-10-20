package com.atguigu.gmall.oms.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.vo.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVO submitVO, Long userId) {
        // 新增订单表
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(userId);
        orderEntity.setOrderSn(submitVO.getOrderToken());
        orderEntity.setDeliveryCompany(submitVO.getDelivery_company());
        orderEntity.setStatus(0);
        orderEntity.setCreateTime(new Date());
        orderEntity.setPayType(submitVO.getPayType());
        orderEntity.setTotalAmount(submitVO.getTotalPrice());
        orderEntity.setSourceType(0);
        // TODO; 自己完成查询并设置 submitVO.getAddressId();

        this.save(orderEntity);

        List<OrderItemVO> items = submitVO.getItems();
        // 新增订单详情表
        items.forEach(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.skuInfo(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();

            orderItemEntity.setCategoryId(skuInfoEntity.getCatalogId());
            orderItemEntity.setOrderId(orderEntity.getId());
            orderItemEntity.setOrderSn(submitVO.getOrderToken());
            orderItemEntity.setSpuId(skuInfoEntity.getSpuId());
            orderItemEntity.setSkuId(skuInfoEntity.getSkuId());
            orderItemEntity.setSkuName(skuInfoEntity.getSkuName());
            orderItemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
            orderItemEntity.setSkuQuantity(item.getCount());
            // TODO:
            this.orderItemDao.insert(orderItemEntity);
        });

        this.amqpTemplate.convertAndSend("ORDER-EXCHANGE", "order.ttl", submitVO.getOrderToken());

        return orderEntity;
    }

    @Override
    public int closeOrder(String orderToken) {

        // 根据订单号，查询订单
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));

        // 判断，如果订单依然处于未付款状态，关单
        if (orderEntity.getStatus() == 0) {
            return this.orderDao.close(orderToken);
        }
        return 0;
    }

    @Override
    public int paySuccess(String orderToken) {
        // 根据订单号，查询订单
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));

        // 判断，如果订单依然处于未付款状态，关单
        if (orderEntity.getStatus() == 0) {
            return this.orderDao.paySuccess(orderToken);
        }
        return 0;
    }

}