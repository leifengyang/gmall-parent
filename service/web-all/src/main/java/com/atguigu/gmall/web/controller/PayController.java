package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Controller
public class PayController {


    @Autowired
    OrderFeignClient orderFeignClient;
    /**
     * 支付信息确认页
     * orderId=776479833227001856
     * @return
     */
    @GetMapping("/pay.html")
    public String payPage(Model model,
                          @RequestParam("orderId") Long orderId){

        //id,totalAmount
        Result<OrderInfo> orderInfo = orderFeignClient.getOrderInfo(orderId);
        Date ttl = orderInfo.getData().getExpireTime();
        Date cur = new Date();
        if(cur.before(ttl)){
            //订单未过期，可以展示支付页
            model.addAttribute("orderInfo",orderInfo.getData());
            return "payment/pay";
        }

        return "payment/error";

    }


    @GetMapping("/pay/success.html")
    public String paySuccessPage(){

        return "payment/success";
    }
}
