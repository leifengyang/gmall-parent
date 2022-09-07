package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class CartController {


    @Autowired
    CartFeignClient cartFeignClient;

//    public static final ConcurrentHashMap<Thread,String> map = new ConcurrentHashMap<>();
//
//    public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 添加商品到购物车
     * @param skuId
     * @param skuNum
     * @return
     *
     * ThreadLocal：
     */
    @GetMapping("/addCart.html")
    public String addCarthtml(@RequestParam("skuId") Long skuId,
                              @RequestParam("skuNum") Integer skuNum,
                              Model model){

        //SpringMVC每次收到请求以后，这个请求默认就和线程绑定好了


        //1、把指定商品添加到购物车
        System.out.println("web-all 获取到的用户id：");
        Result<SkuInfo> result = cartFeignClient.addToCart(skuId, skuNum);
        model.addAttribute("skuInfo",result.getData());
        model.addAttribute("skuNum",skuNum);

        return "cart/addCart";
    }
}
