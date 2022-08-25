package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Sku管理
 */
@RequestMapping("/admin/product")
@RestController
public class SkuController {




    @Autowired
    SkuInfoService skuInfoService;

    /**
     * sku分页查询
     * @return
     */
    @GetMapping("/list/{pn}/{ps}")
    public Result getSkuList(@PathVariable("pn") Long pn,
                             @PathVariable("ps") Long ps){
        Page<SkuInfo> page =new Page<>(pn,ps);
        Page<SkuInfo> result = skuInfoService.page(page);
        return Result.ok(result);
    }


    /**
     * 接前端的json数据，可以使用逆向方式生成vo【和前端对接的JavaBean】
     * https://www.json.cn/json/json2java.html  根据json模型生成vo
     *
     *
     * @param info
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSku(@RequestBody SkuInfo info){

        //sku的大保存
        skuInfoService.saveSkuInfo(info);

        return Result.ok();
    }

    /**
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId")Long skuId){
        skuInfoService.cancelSale(skuId);
        return Result.ok();
    }

    /**
     * 商品上架
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId")Long skuId){
        skuInfoService.onSale(skuId);
        return Result.ok();
    }


}
