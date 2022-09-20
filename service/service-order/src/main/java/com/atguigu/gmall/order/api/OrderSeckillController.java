package com.atguigu.gmall.order.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/inner/rpc/order")
@RestController
public class OrderSeckillController {



    @Autowired
    OrderInfoService orderInfoService;
    /**
     * 保存秒杀单
     * @param info
     * @return
     */
    @PostMapping("/seckillorder/submit")
    public Result<Long> submitSeckillOrder(@RequestBody OrderInfo info){

        Long orderId = orderInfoService.submitSeckillOrder(info);
        return Result.ok(orderId);
    }
}
