package com.atguigu.gmall.order.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.vo.order.OrderWareMapVo;
import com.atguigu.gmall.model.vo.order.WareChildOrderVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 拆单
 */
@Slf4j
@RequestMapping("/api/order")
@RestController
public class OrderSpiltController {


    @Autowired
    OrderBizService orderBizService;
    /**
     * 拆单
     * @return
     */
    @PostMapping("/orderSplit")
    public List<WareChildOrderVo> orderSplit(OrderWareMapVo params){
        log.info("订单执行拆单:{}",params);
        //把这个大订单，拆分成两个子订单（保存数据库）
        return orderBizService.orderSplit(params);
    }
}
