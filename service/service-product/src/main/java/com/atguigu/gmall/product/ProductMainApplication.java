package com.atguigu.gmall.product;



import com.atguigu.gmall.common.annotation.EnableThreadPool;
import com.atguigu.gmall.common.config.RedissonAutoConfiguration;
import com.atguigu.gmall.common.config.Swagger2Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * 1、SpringBoot如何抽取了Redis的配置。
 *   1)、写一些自动配置类，RedisAutoConfiguration，把所有未来用的组件都用这个配置类自动放到容器中
 *   2)、写一个专门用来绑定配置文件中配置的属性类  MinioProperties
 *
 * 默认只扫描 主程序所在的包和子包
 *  //主程序： com.atguigu.gmall.product
 *  //其他：   com.atguigu.gmall.common.config
 *  1、批量导入：@ComponentScan("com.atguigu.gmall.common.config")
 *  2、批量导入： @SpringBootApplication(scanBasePackages = "com.atguigu.gmall")
 *  3、精准导入：@Import({Swagger2Config.class})
 */




@EnableScheduling
@EnableThreadPool
@Import({Swagger2Config.class})
@MapperScan("com.atguigu.gmall.product.mapper") //自动扫描这个包下的所有Mapper接口
@SpringCloudApplication
public class ProductMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductMainApplication.class,args);
    }
}
