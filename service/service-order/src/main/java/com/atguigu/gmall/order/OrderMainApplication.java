package com.atguigu.gmall.order;

import com.atguigu.gmall.annotation.EnableAppRabbit;
import com.atguigu.gmall.common.annotation.EnableAutoExceptionHandler;
import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import com.atguigu.gmall.feign.ware.callback.WareFeignClientCallBack;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * SpringBoot 对 RabbitMQ配置了什么
 * 1、RabbitAutoConfiguration
 *   1)、自动配置好的ConnectionFactory
 *   2)、RabbitTemplateConfigurer： XxxxConfigurer；配置器
 *      自定义RabbitTemplate的配置。想改RabbitTemplate的配置
 *      两种办法定制RabbitTemplate：
 *          1）、给容器中放一个自己的 RabbitTemplateConfigurer
 *          2）、给容器中放一个RabbitTemplate
 *
 *   3)、RabbitTemplate：          操作Rabbit收发消息
 *   4)、AmqpAdmin：               操作MQ服务器，管理MQ的队列、交换机
 *   5)、RabbitMessagingTemplate： 收发消息的。对RabbitTemplate的再次封装，自动消息转换逻辑
 *
 *
 */

@Import({WareFeignClientCallBack.class})
@EnableAppRabbit
@EnableTransactionManagement
@EnableAutoExceptionHandler
@EnableAutoFeignInterceptor //开启feign 用户id透传拦截器
@EnableFeignClients({
        "com.atguigu.gmall.feign.cart",
        "com.atguigu.gmall.feign.user",
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.ware"
})
@MapperScan("com.atguigu.gmall.order.mapper")
@SpringCloudApplication
public class OrderMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class,args);
    }
}
