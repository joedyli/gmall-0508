package com.atguigu.gmall.wms.listener;

import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK-UNLOCK-QUEUE", durable = "true"),
            exchange = @Exchange(value = "STOCK-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unlockStock(String orderToken){
        wareSkuService.unlockStock(orderToken);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK-MINUS-QUEUE", durable = "true"),
            exchange = @Exchange(value = "STOCK-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minusStock(String orderToken){
        wareSkuService.minus(orderToken);
    }
}
