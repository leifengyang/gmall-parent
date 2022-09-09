package com.atguigu.gmall.user.observer;


import java.util.ArrayList;
import java.util.List;

/**
 * 发布者
 */
public class Sgg {

    List<Fans> fans = new ArrayList<>();

    public void publish(String vname){
        System.out.println("硅谷最新出品："+vname);
        //通知所有订阅者（观察者）
        for (Fans fan : fans) {
            fan.receive(vname);
        }
    }

    public void guanzhuwo(Fans fan) {
        fans.add(fan);
    }
}
