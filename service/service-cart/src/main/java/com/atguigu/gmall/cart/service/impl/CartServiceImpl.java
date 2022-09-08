package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public SkuInfo addToCart(Long skuId, Integer num) {
        //  cart:user: == hash(skuId,skuInfo)
        //1、决定购物车使用哪个键
        String cartKey = determinCartKey();

        //2、给购物车添加指定商品
        SkuInfo skuInfo = addItemToCart(skuId,num,cartKey);

        return skuInfo;
    }



    /**
     * 根据用户登录信息决定用哪个购物车键
     * @return
     */
    @Override
    public String determinCartKey() {
        UserAuthInfo info = AuthUtils.getCurrentAuthInfo();
        String cartKey = SysRedisConst.CART_KEY;
        if(info.getUserId()!=null){
            //用户登录了
            cartKey = cartKey+""+info.getUserId();
        }else {
            //用户未登录用临时id
            cartKey = cartKey+""+info.getUserTempId();
        }
        return cartKey;
    }


    @Override
    public SkuInfo addItemToCart(Long skuId, Integer num, String cartKey) {
        // key(cartKey) - hash(skuId - skuInfo)
        //1、如果这个skuId之前没有添加过，就新增。还需要远程调用查询当前信息

        //2、如果这个skuId之前有添加过，就修改skuId对应的商品的数量


        return null;
    }
}
