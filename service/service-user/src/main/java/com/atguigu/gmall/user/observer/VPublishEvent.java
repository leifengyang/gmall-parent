package com.atguigu.gmall.user.observer;

import org.springframework.context.ApplicationEvent;

/**
 * 自定义一个事件
 */
public class VPublishEvent  extends ApplicationEvent {

    public VPublishEvent(Object source) {
        super(source);
    }
}
