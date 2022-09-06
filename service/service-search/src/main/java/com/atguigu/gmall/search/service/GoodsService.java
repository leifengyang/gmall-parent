package com.atguigu.gmall.search.service;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;

public interface GoodsService {
    void saveGoods(Goods goods);

    void deleteGoods(Long skuId);

    SearchResponseVo search(SearchParamVo paramVo);

    void updateHotScore(Long skuId, Long score);
}
