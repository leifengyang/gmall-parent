package com.atguigu.gmall.product.config.minio;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@ConfigurationProperties(prefix = "app.minio")
//和配置文件绑定的
//自动把配置文件中 app.minio 下配置的每个属性全部和这个JavaBean的属性一一对应
@Component
@Data
public class MinioProperties {

    String endpoint;
    String ak;
    String sk;
    String bucketName;

    //以后加配置。配置文件中直接加，别忘了属性类加个属性。
    //以前的代码一个不改，以后的代码都能用
    //设计模式：   对新增开放，对修改关闭【开闭原则】
}
