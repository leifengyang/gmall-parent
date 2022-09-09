package com.atguigu.gmall.user.observer;

import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObsMain {
    public static void aaa(String[] args) throws InterruptedException {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        //流水线车间
        // 物料 ==== 流水线 === 操作1 ===== 操作2 ==== 结束
        //         3====
        // 物料 === 3====
        //         3====
        integers.stream()
                .parallel().
                map(integer -> {
                    System.out.println(Thread.currentThread()+":第一层正在处理："+integer);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread()+"第一层处理结束："+integer);
                    return integer+">";
                })
                .map(s -> {
                    System.out.println(Thread.currentThread()+"第二层正在处理："+s);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread()+"第二层处理结束："+s);
                    return s+"=";
                })
                .collect(Collectors.toList());

        Thread.sleep(10000000);

    }

    public static void haha(String[] args) {

        //观察者
        Fans fans1 = new Fans("张三");
        Fans fans2 = new Fans("李四");
        Fans fans3 = new Fans("王五");




        //视频发布者
        Sgg sgg = new Sgg();





        //1、订立契约
        sgg.guanzhuwo(fans1);
        sgg.guanzhuwo(fans2);
        sgg.guanzhuwo(fans3);

        //2、
        sgg.publish("尚品汇..");


        //3、
        // 1）、Spring的事件机制

        // 2）、监听器机制
//        void started(ConfigurableApplicationContext context) {
//            for (SpringApplicationRunListener listener : this.listeners) {
//                listener.started(context);
//            }
//        }


    }
}
