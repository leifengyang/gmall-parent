package com.atguigu.gmall.seckill;


import com.atguigu.gmall.annotation.EnableAppRabbit;
import com.atguigu.gmall.common.annotation.EnableAutoExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableAppRabbit
@EnableAutoExceptionHandler
@EnableScheduling
@MapperScan("com.atguigu.gmall.seckill.mapper")
@SpringCloudApplication
public class SeckillMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillMainApplication.class,args);
    }
}
