package com.atguigu.gmall.product.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 商品详情数据库层操作
 */
@RequestMapping("/api/inner/rpc/product")
@RestController
public class SkuDetaiApiController {


    @Autowired
    SkuInfoService skuInfoService;
    /**
     * 数据库层真正查询商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/{skuId}")
    public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId){


        //准备查询所有需要的数据
        SkuDetailTo skuDetailTo = skuInfoService.getSkuDetail(skuId);



        return Result.ok(skuDetailTo);
    }

}
