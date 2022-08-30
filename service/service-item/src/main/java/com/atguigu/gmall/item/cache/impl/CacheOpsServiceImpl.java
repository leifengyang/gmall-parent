package com.atguigu.gmall.item.cache.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.item.cache.CacheOpsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 封装了缓存操作
 */
@Service
public class CacheOpsServiceImpl implements CacheOpsService {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;


    /**
     * 从缓存中获取一个数据，并转成指定类型的对象
     * @param cacheKey
     * @param clz
     * @param <T>
     * @return
     */
    @Override
    public <T> T getCacheData(String cacheKey, Class<T> clz) {
        String jsonStr = redisTemplate.opsForValue().get(cacheKey);
        //引入null值缓存机制。
        if(SysRedisConst.NULL_VAL.equals(jsonStr)){
            return null;
        }
        T t = Jsons.toObj(jsonStr, clz);
        return t;
    }

    @Override
    public boolean bloomContains(Long skuId) {
        RBloomFilter<Object> filter = redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);
        return filter.contains(skuId);
    }

    @Override
    public boolean tryLock(Long skuId) {
        //1、准备锁用的key    lock:sku:detail:50
        String lockKey = SysRedisConst.LOCK_SKU_DETAIL+skuId;
        RLock lock = redissonClient.getLock(lockKey);
        //2、尝试加锁
        boolean tryLock = lock.tryLock();
        return tryLock;
    }

    @Override
    public void saveData(String cacheKey, Object fromRpc) {
        if(fromRpc == null){
            //null值缓存短一点时间
            redisTemplate.opsForValue().set(cacheKey,
                    SysRedisConst.NULL_VAL,
                    SysRedisConst.NULL_VAL_TTL,
                    TimeUnit.SECONDS);
        }else {
            String str = Jsons.toStr(fromRpc);
            redisTemplate.opsForValue().set(cacheKey,
                    str,
                    SysRedisConst.SKUDETAIL_TTL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void unlock(Long skuId) {
        String lockKey = SysRedisConst.LOCK_SKU_DETAIL+skuId;
        RLock lock = redissonClient.getLock(lockKey);

        //解掉这把锁
        lock.unlock();
    }
}
