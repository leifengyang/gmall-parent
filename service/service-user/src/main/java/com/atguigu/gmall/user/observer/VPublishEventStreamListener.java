package com.atguigu.gmall.user.observer;


import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VPublishEventStreamListener {


    @EventListener(VPublishEvent.class)
    public void listenVPublishEvent(VPublishEvent event){
        System.out.println("视频流业务监听到事件："+event+"； 正在推流给服务器");
    }
}
