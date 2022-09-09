package com.atguigu.gmall.user.observer;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 订阅者
 */
@AllArgsConstructor
@Data
public class Fans {

    private String userName;

    public void receive(String vname){
        System.out.println(userName+"： 收到了硅谷大礼包："+vname);
    }
}
