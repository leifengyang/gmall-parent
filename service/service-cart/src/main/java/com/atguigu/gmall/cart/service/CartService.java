package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.product.SkuInfo;

public interface CartService {
    /**
     * 添加一个商品到购物车
     * @param skuId
     * @param num
     * @return
     */
    SkuInfo addToCart(Long skuId, Integer num);

    /**
     * 根据用户登录信息决定用哪个购物车键
     * @return
     */
    String determinCartKey();

    /**
     * 把指定商品添加到指定购物车
     * @param skuId
     * @param num
     * @param cartKey
     * @return
     */
    SkuInfo addItemToCart(Long skuId, Integer num, String cartKey);
}
