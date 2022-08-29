package com.atguigu.gmall.product.api;


import com.atguigu.gmall.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * 组件默认是单实例，Controller一直在
 */
@RestController
public class HelloController {

    //属性一直在； GC，根可达
    Map<String,String> aa = new HashMap<>();
    @GetMapping("/haha/hello")
    public Result hello(){
        String s = UUID.randomUUID().toString();
        aa.put(s,s);
        return Result.ok();
    }
}
