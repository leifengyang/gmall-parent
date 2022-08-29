package com.atguigu.gmall.common.config.threadpool;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 配置线程池
 */
//1、AppThreadPoolProperties 里面的所有属性和指定配置绑定
//2、AppThreadPoolProperties 组件自动放到容器中
//开启自动化属性绑定配置
@EnableConfigurationProperties(AppThreadPoolProperties.class)
@Configuration
public class AppThreadPoolAutoConfiguration {


    @Autowired
    AppThreadPoolProperties threadPoolProperties;

    @Value("${spring.application.name}")
    String applicationName;

    @Bean
    public ThreadPoolExecutor coreExecutor(){
        // -Xmx100m 100mb：  内存合理规划使用
        //压力测试：  1亿/1万/1个   500mb
//        new ArrayBlockingQueue(10)： 底层队列是一个数组
//        new LinkedBlockingDeque(10)： 底层是一个链表
        //数组与链表？ -- 检索、插入
        //数组是连续空间，链表不连续（利用碎片化空间）
        /**
         * int corePoolSize,  核心线程池： cpu核心数   4
         * int maximumPoolSize, 最大线程数：          8
         * long keepAliveTime,  线程存活时间
         * TimeUnit unit,      时间单位
         * BlockingQueue<Runnable> workQueue, 阻塞队列：大小需要合理
         * ThreadFactory threadFactory,  线程工厂。 自定义创建线程的方法
         * RejectedExecutionHandler handler
         *
         * //  2000/s：队列大小根据接口吞吐量标准调整
         */
        //配置可抽取
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolProperties.getCore(),
                threadPoolProperties.getMax(),
                threadPoolProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(threadPoolProperties.getQueueSize()), //队列的大小由项目最终能占的最大内存决定
                new ThreadFactory() { //负责给线程池创建线程
                    int i = 0; //记录线程自增id
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r); //创建的线程要接受任务
                        thread.setName(applicationName+"[core-thread-"+ i++ +"]");
                        return thread;
                    }
                },
                //生产环境用 CallerRuns，保证就算线程池满了，
                // 不能提交的任务，由当前线程自己以同步的方式执行
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        //每个线程的线程名都是默认的。
        return executor;
    }


}
