package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RibbtMqConfig {

    @Bean
    public Exchange exchange(){
        return new TopicExchange("ORDER-EXCHANGE", true, false);
    }

    @Bean
    public Queue ttlQueue(){
        Map<String, Object> map = new HashMap<>();
        map.put("x-message-ttl", 150000);
        map.put("x-dead-letter-exchange", "ORDER-EXCHANGE");
        map.put("x-dead-letter-routing-key", "order.dead");
        return new Queue("ORDER-TTL-QUEUE", true, false, false, map);
    }

    @Bean
    public Binding ttlBinding(){

        return new Binding("ORDER-TTL-QUEUE", Binding.DestinationType.QUEUE, "ORDER-EXCHANGE", "order.ttl", null);
    }

    @Bean
    public Queue deadQueue(){
        return new Queue("ORDER-DEAD-QUEUE", true, false, false, null);
    }

    @Bean
    public Binding deadBinding(){
        return new Binding("ORDER-DEAD-QUEUE", Binding.DestinationType.QUEUE, "ORDER-EXCHANGE", "order.dead", null);
    }
}
