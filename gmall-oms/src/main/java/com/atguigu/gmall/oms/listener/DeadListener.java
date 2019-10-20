package com.atguigu.gmall.oms.listener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeadListener {

//    @RabbitListener(queues = {"ORDER-DEAD-QUEUE"})
    public void test(String msg, Channel channel, Message message) throws IOException {

        try {
            System.out.println(msg);
//            int i = 1/0;
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
