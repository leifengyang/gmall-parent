package com.atguigu.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * 主启动类
 */
//@EnableCircuitBreaker  //开启服务熔断降级、流量保护 [1、导入jar  2、使用这个注册]
//@EnableDiscoveryClient //开启服务发现[1、导入服务发现jar  2、使用这个注解]
//@SpringBootApplication

@SpringCloudApplication  //以上的合体
public class GatewayMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMainApplication.class,args);
    }
}
