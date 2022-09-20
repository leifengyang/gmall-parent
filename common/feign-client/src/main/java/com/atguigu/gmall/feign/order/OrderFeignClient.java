package com.atguigu.gmall.feign.order;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/inner/rpc/order")
@FeignClient("service-order")
public interface OrderFeignClient {
    /**
     * 获取订单确认页需要的数据
     * @return
     */
    @GetMapping("/confirm/data")
    Result<OrderConfirmDataVo> getOrderConfirmData();

    /**
     * 获取某个订单数据
     * @param orderId
     * @return
     */
    @GetMapping("/info/{orderId}")
    Result<OrderInfo> getOrderInfo(@PathVariable("orderId") Long orderId);


    /**
     * 保存秒杀单
     * @param info
     * @return
     */
    @PostMapping("/seckillorder/submit")
    Result<Long> submitSeckillOrder(@RequestBody OrderInfo info);

}
