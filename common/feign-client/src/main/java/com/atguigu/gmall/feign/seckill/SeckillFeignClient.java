package com.atguigu.gmall.feign.seckill;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/inner/rpc/seckill")
@FeignClient("service-seckill")
public interface SeckillFeignClient {

    @GetMapping("/currentday/goods/list")
    Result<List<SeckillGoods>> getCurrentDaySeckillGoodsList();
    @GetMapping("/good/detail/{skuId}")
    public Result<SeckillGoods> getSeckillGood(@PathVariable("skuId") Long skuId);
}
