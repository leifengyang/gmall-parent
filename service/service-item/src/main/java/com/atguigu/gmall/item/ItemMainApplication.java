package com.atguigu.gmall.item;


import com.atguigu.gmall.common.annotation.EnableThreadPool;
import com.atguigu.gmall.common.config.RedissonAutoConfiguration;
import com.atguigu.gmall.common.config.threadpool.AppThreadPoolAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;


/**
 * 1、公共的配置搬家放到 service-util
 * 2、当前项目依赖了 service-util
 *
 * 当前应用启动只会扫描 ItemMainApplication 所在包的所有组件
 * - com.atguigu.gmall.item.*****
 * - com.atguigu.gmall.common.**
 */


/**
 * 1、RedisAutoConfiguration
 *    给容器中放了 RedisTemplate<Object, Object> 和 StringRedisTemplate
 *    给redis存数据，都是k-v（v有很多类型）【string,jsonstring】
 *    StringRedisTemplate = RedisTemplate<String, String> ；
 *    给redis存数据，key是string，value序列化成字符串
 */


@EnableThreadPool //
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.search"
})
@SpringCloudApplication
public class ItemMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemMainApplication.class,args);
    }

    public static void ddd(String[] args) throws InterruptedException {
        Hello hello = new Hello();

        //先启动...
        new Thread(()->{
            hello.hehe();  //a=false放到工作内存
        }).start();

        //启动a的改变线程
        Thread.sleep(1000);
        new Thread(()->{
            hello.haha();
            System.out.println("主内存是："+hello.a);
        }).start();





        Thread.sleep(10000000);
    }


    static class Hello {
        volatile boolean a = false; //

        public void haha(){
            a = true;
            System.out.println("a已经改了");
        }

        void hehe(){
            //自旋
            //代表每次可以从主存读最新数据,总线发现主内存数据变化，更新工作内存的数据
            while (!a){ //一旦加了volatile,代表每次可以从主存读最新数据

            }
            System.out.println("a=true，程序中断....");
        }
    }
}
