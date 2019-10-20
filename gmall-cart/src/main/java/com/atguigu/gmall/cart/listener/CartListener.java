package com.atguigu.gmall.cart.listener;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class CartListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "gmall:cart:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-CART-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-CART-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteListener(Map<String, Object> map){

        if (CollectionUtils.isEmpty(map)){
            return ;
        }
        // 获取消息
        Long userId = (Long) map.get("userId");
        List<Long> skuIds = (List<Long>)map.get("skuIds");

        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        skuIds.forEach(skuId -> {
            ops.delete(skuId.toString());
        });
    }
}
