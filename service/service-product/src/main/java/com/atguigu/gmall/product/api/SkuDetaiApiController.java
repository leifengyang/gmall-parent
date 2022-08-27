package com.atguigu.gmall.product.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;


/**
 * 商品详情数据库层操作
 */
@RequestMapping("/api/inner/rpc/product")
@RestController
public class SkuDetaiApiController {


    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SpuSaleAttrService spuSaleAttrService;

    @Autowired
    BaseCategory3Service baseCategory3Service;
    /**
     * 数据库层真正查询商品详情
     * @param skuId
     * @return
     */
//    @GetMapping("/skudetail/{skuId}")
//    public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId){
//        //准备查询所有需要的数据
//        SkuDetailTo skuDetailTo = skuInfoService.getSkuDetail(skuId);
//        return Result.ok(skuDetailTo);
//    }

    /**
     * 查询sku的基本信息
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/info/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuInfoService.getDetailSkuInfo(skuId);
        return Result.ok(skuInfo);
    }


    /**
     * 查询sku的图片信息
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/images/{skuId}")
    public Result<List<SkuImage>> getSkuImages(@PathVariable("skuId")Long skuId){
        List<SkuImage> images = skuInfoService.getDetailSkuImages(skuId);
        return Result.ok(images);
    }


    /**
     * 查询sku的实时价格
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/price/{skuId}")
    public Result<BigDecimal> getSku1010Price(@PathVariable("skuId")Long skuId){
        BigDecimal price = skuInfoService.get1010Price(skuId);
        return Result.ok(price);
    }


    /**
     * 查询sku对应的spu定义的所有销售属性名和值。并且标记出当前sku是哪个
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/saleattrvalues/{skuId}/{spuId}")
    public Result<List<SpuSaleAttr>> getSkuSaleattrvalues(@PathVariable("skuId") Long skuId,
                                       @PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> saleAttrList = spuSaleAttrService
                .getSaleAttrAndValueMarkSku(spuId,skuId);
        return Result.ok(saleAttrList);
    }


    /**
     * 查sku组合 valueJson
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/valuejson/{spuId}")
    public Result<String> getSKuValueJson(@PathVariable("spuId") Long spuId){
        String valuejson = spuSaleAttrService.getAllSkuSaleAttrValueJson(spuId);
        return Result.ok(valuejson);
    }


    /**
     * 查分类
     * @param c3Id
     * @return
     */
    @GetMapping("/skudetail/categoryview/{c3Id}")
    public Result<CategoryViewTo> getCategoryView(@PathVariable("c3Id") Long c3Id){

        CategoryViewTo categoryViewTo = baseCategory3Service.getCategoryView(c3Id);
        return Result.ok(categoryViewTo);
    }





}
