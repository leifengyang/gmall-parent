package com.atguigu.gmall.search;


import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


/**
 * Elasticsearch 的自动配置
 * 1、ElasticsearchDataAutoConfiguration
 *   @Import({ ElasticsearchDataConfiguration.BaseConfiguration.class,
 * 		ElasticsearchDataConfiguration.RestClientConfiguration.class,
 * 		ElasticsearchDataConfiguration.ReactiveRestClientConfiguration.class })
 *
 *      ElasticsearchDataConfiguration：做了什么
 *      1)、BaseConfiguration： 基本类型转换器。
 *      2)、RestClientConfiguration：
 *          【ElasticsearchRestTemplate】： 操作ES的模板工具类，给ES发送HTTP请求完成CRUD
 *      3)、ReactiveRestClientConfiguration： 响应式编程
 *
 *
 * 2、ElasticsearchRepositoriesAutoConfiguration：  开启ES的自动仓库
 *      @Import(ElasticsearchRepositoriesRegistrar.class)
 *      @EnableElasticsearchRepositories： 开启ES的自动仓库？
 *      - Person
 *      - PersonRepository： 只需要写接口。 普通的CRUD
 *      - 复杂的自己写DSL
 *
 *      Mybatis-Plus：
 *      - SkuInfo
 *      - SkuInfoMapper：
 *      - 复杂的自己写SQL
 *
 *
 * 3、ElasticsearchRestClientAutoConfiguration
 *    @Import({
 *      ElasticsearchRestClientConfigurations.RestClientBuilderConfiguration.class,
 * 		ElasticsearchRestClientConfigurations.RestHighLevelClientConfiguration.class,
 * 		ElasticsearchRestClientConfigurations.RestClientFallbackConfiguration.class })
 * 	  配置：
 * 	    spring.elasticsearch.rest
 * 	    下面的所有属性，就是Es rest客户端的配置
 *    操作ES的原生客户端 RestHighLevelClient. JDBC.
 *    ElasticsearchRestTemplate 包装了 RestHighLevelClient，所以直接用Template就行
 *
 *
 * 总结：
 *    1）、普通CRUD，写Bean，写接口
 *    2）、复杂CRUD，ElasticsearchRestTemplate 自己调用相关的方法构造复杂的DSL完成功能
 *
 */

@EnableElasticsearchRepositories  //开启ES的自动仓库功能。
@SpringCloudApplication
public class SearchMainApplication {


    public static void main(String[] args) {
//        new ClassPathXmlApplicationContext();
        SpringApplication.run(SearchMainApplication.class,args);
    }
}
