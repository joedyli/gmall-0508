package com.atguigu.gmall.oms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallOmsApplicationTests {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    void contextLoads() {

        this.amqpTemplate.convertAndSend("ORDER-EXCHANGE", "order.ttl", "hello world");
    }

}
