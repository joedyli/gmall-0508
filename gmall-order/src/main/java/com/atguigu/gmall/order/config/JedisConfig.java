package com.atguigu.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class JedisConfig {

    @Bean
    public JedisPool jedisPool(){

        return new JedisPool("172.16.116.100");
    }
}
