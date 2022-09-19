package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillGoodsCacheOpsServiceImpl  implements SeckillGoodsCacheOpsService {

    @Autowired
    StringRedisTemplate redisTemplate;

    private Map<Long,SeckillGoods> goodsCache = new ConcurrentHashMap<>();

    @Override
    public void upSeckillGoods(List<SeckillGoods> list) {
        String date = DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate
                        .boundHashOps(SysRedisConst.CACHE_SECKILL_GOODS + date);

        //数据多缓存一天。为了方便秒杀结束后续定时任务进行统计信息
        hashOps.expire(2, TimeUnit.DAYS);

        list.stream().forEach(seckillGoods -> {
            //1、秒杀商品信息保存到redis
            hashOps.put(seckillGoods.getSkuId()+"", Jsons.toStr(seckillGoods));
            //2、商品的库存数量独立存储
            //seckill:goods:stock:49
            String cacheKey = SysRedisConst.CACHE_SECKILL_GOODS_STOCK + seckillGoods.getSkuId();
            //3、缓存商品的精确库存
            redisTemplate.opsForValue().setIfAbsent(cacheKey,seckillGoods.getStockCount()+"",1,TimeUnit.DAYS);
            //4、本地缓存
            goodsCache.put(seckillGoods.getSkuId(),seckillGoods);
        });

    }

    @Override
    public void clearCache() {
        goodsCache.clear();
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsFromLocal() {
        //1、优先查询本地
        List<SeckillGoods> goods = goodsCache.values().stream()
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());
        //2、本地没有
        if(goods == null || goods.size()==0 ){
            //3、同步远程
            syncLocalAndRedisCache();
            //4、拿到本地数据
            goods = goodsCache.values().stream()
                    .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                    .collect(Collectors.toList());
        }

        return goods;
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsFromRemote() {
        String cacheKey = SysRedisConst.CACHE_SECKILL_GOODS + DateUtil.formatDate(new Date());
        //
        List<Object> values = redisTemplate.opsForHash().values(cacheKey);

        List<SeckillGoods> goods = values.stream()
                .map(str -> Jsons.toObj(str.toString(), SeckillGoods.class))
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());


        return goods;
    }


    @Override
    public void syncLocalAndRedisCache() {
        //1、查到redis数据
        List<SeckillGoods> goods = getSeckillGoodsFromRemote();
        //2、同步到本地
        goods.stream().forEach(item->{
            goodsCache.put(item.getSkuId(),item);
        });
    }

    @Override
    public SeckillGoods getSeckillGoodsDetail(Long skuId) {
        //1、先拿本地缓存
        SeckillGoods goods = goodsCache.get(skuId);

        //2、本地缓存没有
        if(goods == null){
            syncLocalAndRedisCache();
            goods = goodsCache.get(skuId);
        }
        return goods;
    }
}
