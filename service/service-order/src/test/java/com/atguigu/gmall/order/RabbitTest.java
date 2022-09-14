package com.atguigu.gmall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;

@SpringBootTest
public class RabbitTest {


    @Autowired
    RabbitTemplate rabbitTemplate;



    @Test
    void  testSend(){
//        //发送端确认-只能感知MQ能否收到消息（连接正常）
//        rabbitTemplate.setConfirmCallback((CorrelationData correlationData,
//                                           boolean ack,
//                                           String cause)->{
//            if(ack){
//                System.out.println("MQ 交换机收到了消息");
//            }else {
//                System.out.println("MQ收到消息失败。原因："+cause);
//            }
//        });
//
//        //发送端确认-消息必须进队列才算成功
//        rabbitTemplate.setReturnCallback((Message message,
//                                          int replyCode, //312
//                                          String replyText,
//                                          String exchange,
//                                          String routingKey)->{
//            //只要消息给队列存储失败。就会收到通知
//            System.out.println("投递失败的消息：保存到数据库："+message);
//            System.out.println("刚才消息：【"+new String(message.getBody())+"】 replyCode:"+replyCode+"  replyText:"+replyText + " ==>"+exchange+","+routingKey);
//        });


        //默认感知不到失败的消息
        rabbitTemplate.convertAndSend("hahax","haa","123");
    }
}
