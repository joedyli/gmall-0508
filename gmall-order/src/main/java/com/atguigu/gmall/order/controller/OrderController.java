package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.interceptor.UserInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.*;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String SEMAPHORE_PREFIX = "sec:kill:";

    @PostMapping("confirm")
    public Resp<OrderConfirmVO> confirm(@RequestBody Map<Long, Integer> map){

        OrderConfirmVO vo = this.orderService.confirm(map);
        return Resp.ok(vo);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO) throws AlipayApiException {

        OrderSubmitResponseVO orderSubmitResponseVO = this.orderService.submit(orderSubmitVO);

        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSubmitResponseVO.getOrderToken());
        payVo.setTotal_amount(orderSubmitResponseVO.getOrderEntity().getTotalAmount().toString());
        payVo.setSubject("谷粒商城收银台");
        payVo.setBody("谷粒商城在线支付");

        String pay = this.alipayTemplate.pay(payVo);
        System.out.println(pay);

        return Resp.ok(pay);
    }

    @RequestMapping("pay/alipay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo){

        this.orderService.paySuccess(payAsyncVo.getOut_trade_no());

        return Resp.ok(null);
    }

    @GetMapping("miaosha/{skuId}")
    public Resp<Object> miaosha(@PathVariable("skuId")Long skuId) throws InterruptedException {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null){
            String stockString = this.redisTemplate.opsForValue().get("sec:stock:" + skuId);
            RSemaphore semaphore = this.redissonClient.getSemaphore(SEMAPHORE_PREFIX + skuId);
            semaphore.trySetPermits(Integer.valueOf(stockString));

            SecKillVO secKillVO = new SecKillVO();
            secKillVO.setSkuId(skuId);
            secKillVO.setUserId(userInfo.getUserId());
            this.amqpTemplate.convertAndSend("ORDER.EXCHANGE", "order.seckill", JSON.toJSONString(secKillVO));

            semaphore.acquire();
        }
        return Resp.ok(null);
    }

    @RequestMapping("test")
    public String test() throws InterruptedException {

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                String stockString = this.redisTemplate.opsForValue().get("sec:stock:120");
                RSemaphore semaphore = this.redissonClient.getSemaphore("semaphore");
                semaphore.trySetPermits(Integer.valueOf(stockString));
                try {
                    semaphore.acquire();
                    System.out.println("占用了一个车位" + Thread.currentThread().getName());
                    TimeUnit.SECONDS.sleep(3 + ((int) (Math.random()*5)));
                    System.out.println("挺了3秒钟！");
                    System.out.println("释放了一个车位" + Thread.currentThread().getName());
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
        return "....";
    }

    public static void main(String[] args) {

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                Semaphore semaphore = new Semaphore(3);
                try {
                    semaphore.acquire();
                    System.out.println("占用了一个车位" + Thread.currentThread().getName());
                    TimeUnit.SECONDS.sleep(3 + ((int) (Math.random()*5)));
                    System.out.println("挺了3秒钟！");
                    System.out.println("释放了一个车位" + Thread.currentThread().getName());
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
    }

}
