package com.atguigu.gmall.item.service;

import com.atguigu.gmall.model.to.SkuDetailTo;

public interface SkuDetailService {
    SkuDetailTo getSkuDetail(Long skuId);

    /**
     * 更新商品热度
     * @param skuId
     */
    void updateHotScore(Long skuId);
}
