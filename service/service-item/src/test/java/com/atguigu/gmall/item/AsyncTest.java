package com.atguigu.gmall.item;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTest {


    static ExecutorService executor = Executors.newFixedThreadPool(4);


    /**
     * 多任务组合
     *  allOf,anyOf
     *
     * @param args
     */
    public static void allof(String[] args) throws Exception {
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("aaa");
        });

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 222;
        });

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            System.out.println("dddd");
        });


        //这三个任务全部完成，做另一个事情
//        CompletableFuture.allOf(future1,future2,future3)
//                .thenRun(()->{
//                    System.out.println("都完了...");
//                });

        CompletableFuture<Void> future = CompletableFuture
                .allOf(future1, future2, future3);

        future.join();

        System.out.println("哈哈");

        Thread.sleep(1000000L);

    }

    /**
     * thenXXX编排任务
     *
     * CompletableFuture<Void>
     * CompletableFuture<Integer>
     *
     *     1、thenRun()\thenRunAsync() future.thenXXXX() ： 接下来干什么 CompletableFuture<Void>
     *          thenRun(runnable)：              不用异步跑任务，而是用主线程
     *          thenRunAsync(runnable):          用异步跑任务，使用默认ForkJoin线程池
     *          thenRunAsync(runnable,executor)
     *        接下来干活用不到上一步的结果，自己运行完也不返回任何结果 CompletableFuture<Void>
     *     2、thenAccept()\thenAcceptAsync()： CompletableFuture<Void>
     *          thenAccept(consumer):       拿到上一步结果，不用异步跑任务，而是用主线程
     *          thenAcceptAsync(consumer):  拿到上一步结果，用异步跑任务，使用默认ForkJoin线程池
     *          thenAcceptAsync(consumer,executor)  拿到上一步结果，用异步跑任务，使用指定线程池
     *     3、thenApply()\thenApplyAsync()：  拿到上一步结果，还能自己返回新结果
     *          thenApply(function)：        拿到上一步结果，不用异步跑任务，而是用主线程，并返回自己的计算结果
     *          thenApplyAsync(function)：   拿到上一步结果，用异步跑任务，用默认线程池，并返回自己的计算结果
     *          thenApplyAsync(function,executor)： 拿到上一步结果，用异步跑任务，用指定线程池，并返回自己的计算结果
     *
     *  thenRun： 不接收上一次结果，无返回值
     *  thenAccept：接收上一次结果，无返回值
     *  thenApply： 接收上一次结果，有返回值
     * @param args
     */
    public static void thenXXX(String[] args) throws Exception {
        //1、1+1   2+3   +5
        //    +3
       CompletableFuture.supplyAsync(()->{
           return 2;
       }).thenApplyAsync((t)->{
           return t+3;
       },executor).thenApply((t)->{
           return t*6;
       }).thenAcceptAsync((t)->{
           System.out.println("把"+t+"保存到数据库");
       }).whenComplete((t,u)->{
           if(u!=null){
               //记录日志
           }
           System.out.println("执行结束，记录日志");
       });





        Thread.sleep(1000000L);
    }

    /**
     * 测试启动异步任务
     * @param args
     * @throws Exception
     */
    public static void startAsync(String[] args) throws Exception {


        System.out.println(Thread.currentThread().getName() + "： 主线程开始");
        //1、启动异步任务
        // runAsync();
        //    1）、CompletableFuture.runAsync(Runnable) 返回 CompletableFuture<Void>;
        //             使用默认线程池(ForkJoinPool)，启动一个Runnable任务进行执行，没有返回结果
        //    2）、CompletableFuture.runAsync(Runnable,指定线程池) 返回 CompletableFuture<Void>;
        //             使用指定线程池，启动一个Runnable任务进行执行，没有返回结果
        // supplyAsync();
        //    1）、CompletableFuture.supplyAsync(Runnable) 返回 CompletableFuture<Integer>
        //             使用默认线程池(ForkJoinPool)，启动一个Runnable任务进行执行，有返回结果
        //    2）、CompletableFuture.supplyAsync(Runnable,指定线程池)
        //             使用指定线程池，启动一个Runnable任务进行执行，有返回结果

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() +"：哈哈");
            return 2; //底层Callable
        },executor);

        Integer integer = future.get(); //阻塞等待future异步结果
        Integer integer1 = future.get(1, TimeUnit.MINUTES); //限时等待
        System.out.println("异步结果："+integer);

        Thread.sleep(1000000L);

    }


    public static void jieshao(String[] args) throws InterruptedException {
        //1、异步编排 CompletableFuture jdk8的一个新特性
        //把异步任务编排起来【skuId】
        //查基本信息(skuId)、查图片(skuId)、查分类(c3Id)、
        //查销售属性(skuId,spuId)、查实时价格(skuId)、查skuvaluejson(spuId)
        //1）、等所有的异步任务全部执行完成，查到结果以后，把整个数据聚合返回
        //2）、所有异步任务之间关系比较复杂
        //查基本信息(skuId): skuInfo(spuId、c3Id)
        //   -- 查分类(c3Id):
        //   -- 查销售属性(skuId,spuId)
        //   -- 查skuvaluejson(spuId)
        //查图片(skuId)
        //查实时价格(skuId)
        //  同一层级异步执行，父子层级等待。
        //  以前的技术来解决异步之间的编排关系。想想都复杂



        //1、启动异步任务
        //    - CompletableFuture.runAsync()   ：CompletableFuture<Void> future
        //    - CompletableFuture.supplyAsync()：
        //2、编排他们的关系
        //     future.whenComplete()


    }
}
