package com.atguigu.gmall.search.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;
import com.atguigu.gmall.search.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/inner/rpc/search")
@RestController
public class SearchApiController {

    @Autowired
    GoodsService goodsService;
    /**
     * 保存商品信息到es
     * @param goods
     * @return
     */
    @PostMapping("/goods")
    public Result saveGoods(@RequestBody Goods goods){
        goodsService.saveGoods(goods);
        return Result.ok();
    }

    @DeleteMapping("/goods/{skuId}")
    public Result deleteGoods(@PathVariable("skuId") Long skuId){
        goodsService.deleteGoods(skuId);
        return Result.ok();
    }


    /**
     * 商品检索
     * @param paramVo
     * @return
     */
    @PostMapping("/goods/search")
    public Result<SearchResponseVo> search(@RequestBody SearchParamVo paramVo){
        SearchResponseVo responseVo = goodsService.search(paramVo);
        return Result.ok(responseVo);
    }
}
