package com.atguigu.gmall.product.config.minio;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio的自动配置类
 */
@Configuration  //是容器中的组件
public class MinioAutoConfiguration {



    @Autowired
    MinioProperties minioProperties;
    /**
     * 未来想要进行文件上传的人。自动注入 MinioClient
     * @return
     */
    @Bean
    public MinioClient minioClient() throws Exception {

        //1、创建Minio客户端
        MinioClient minioClient = new MinioClient(
                minioProperties.getEndpoint(),
                minioProperties.getAk(),
                minioProperties.getSk()
        );

        String bucketName = minioProperties.getBucketName();
        if(!minioClient.bucketExists(bucketName)){
            minioClient.makeBucket(bucketName);
        }

        return minioClient;
    }
}
