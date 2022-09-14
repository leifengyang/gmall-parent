package com.atguigu.gmall.order.listener;


import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MQListener {



    //Map搬到redis。 counter:  value(id - count)
    private ConcurrentHashMap<String,AtomicInteger> counter = new ConcurrentHashMap<>();


    public void listenHaha(Message message, Channel channel) throws IOException {
        //1、每个消息发送的时候，就该给个唯一标志
        String content = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        counter.putIfAbsent(content,new AtomicInteger(0));
        try{
            System.out.println("收到的消息："+content);
            //处理业务 12 TODO
            int i = 10/0;
            channel.basicAck(deliveryTag,false); //不要批量回复
        }catch (Exception e){
            log.error("消息消费失败：{}",content);
            AtomicInteger integer = counter.get(content);
            System.out.println(deliveryTag+"加到："+integer);
            if(integer.incrementAndGet() <= 10){
                //重新存储这个消息，待下个人继续处理
                channel.basicNack(deliveryTag,false,true);
            }else{
                //已经超过最大重试次数
                //TODO 把超过重试次数的消息，放到数据库专门一张表，重试失败消息表。
                //TODO 人工补偿（1、人工修改  2、业务Bug修改完成以后重新发送这些消息让业务继续消费）。
                log.error("{} 消息重试10次依然失败，已经记录到数据库等待人工处理",content);
                channel.basicNack(deliveryTag,false,false);
                counter.remove(content);
            }
        }
    }
}
