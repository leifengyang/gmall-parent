package com.atguigu.gmall.product;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
public class ThreadpoolTest {


    @Autowired
    ThreadPoolExecutor executor;

    @Test
    void testPool() throws InterruptedException {

        for(int i=0;i < 100;i++){
            executor.submit(()->{
                System.out.println(Thread.currentThread().getName()+":"+ UUID.randomUUID().toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // service-product[core-thread-2]:  9a1a5b74-6ff1-49bf-84ba-32db64ea808f

        // service-item[core-thread-3]:  00248c06-6df8-4b59-b230-b855eb136710

        Thread.sleep(10000000000L);
    }
}
