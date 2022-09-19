package com.atguigu.gmall.seckill.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/activity/seckill/auth")
@RestController
public class SeckillRestController {


    @Autowired
    SeckillBizService bizService;

    /**
     * 生成秒杀码，以便于真实隐藏秒杀地址
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillCode(@PathVariable("skuId") Long skuId){


        String code = bizService.generateSeckillCode(skuId);
        return Result.ok(code);
    }


    /**
     * 秒杀预下单： 开始秒杀排队
     * @return
     */
    @PostMapping("/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId,
                               @RequestParam("skuIdStr") String skuIdStr){


        ResultCodeEnum codeEnum = bizService.seckillOrder(skuId, skuIdStr);
        //1、秒杀码是否合法能否进行秒杀
        //2、走整个秒杀流程
        //3、告诉页面结果

        return Result.build("",codeEnum); //响应成功 200 码。
    }


    /**
     * 检查秒杀订单的状态
     */
    @GetMapping("/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId){

        ResultCodeEnum resultCodeEnum = bizService.checkSeckillOrderStatus(skuId);
        return Result.build("",resultCodeEnum);
    }



}
