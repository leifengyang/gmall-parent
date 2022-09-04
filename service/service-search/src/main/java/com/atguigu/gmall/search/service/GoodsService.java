package com.atguigu.gmall.search.service;

import com.atguigu.gmall.model.list.Goods;

public interface GoodsService {
    void saveGoods(Goods goods);

    void deleteGoods(Long skuId);
}
