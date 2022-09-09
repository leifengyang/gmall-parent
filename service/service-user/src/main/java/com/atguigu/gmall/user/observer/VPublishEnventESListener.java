package com.atguigu.gmall.user.observer;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


/**
 * 设计模式： 开闭原则
 *      【对新增开放，对修改关闭】
 */
@Component
public class VPublishEnventESListener {

    //监听事件
    @EventListener(VPublishEvent.class)
    public void listenVPublishEvent(VPublishEvent event){
        System.out.println("检索业务监听到事件："+event+"； 正在保存信息到ES");
    }
}
