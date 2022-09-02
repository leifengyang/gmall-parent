package com.atguigu.gmall.feign.item;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/inner/rpc/item")
@FeignClient("service-item")
public interface SkuDetailFeignClient {

    @GetMapping("/skudetail/{skuId}")
    Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId")Long skuId);
}
