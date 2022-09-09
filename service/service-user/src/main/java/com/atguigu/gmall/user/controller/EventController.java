package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.user.observer.VPublishEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {


    @Autowired
    SimpleApplicationEventMulticaster multicaster; //事件派发器
    /**
     * 1、一个视频发布以后
     *   - 1. 把视频流存储到流服务器
     *   - 2. 把视频资料缓存到es
     *   - 3. 把视频推送给关注我的人
     *   - 4. 增加尚硅谷公众号活跃度
     * @param name
     * @return
     */
    @GetMapping("/publish/v/{name}")
    public String publish(@PathVariable("name") String name){
        //TODO 视频保存到库
        //事件机制。发出去一个事件（发一个消息）

        //完全解耦的编码方式
        multicaster.multicastEvent(new VPublishEvent(name)); //派发事件

        return "发布："+name;
    }


}
