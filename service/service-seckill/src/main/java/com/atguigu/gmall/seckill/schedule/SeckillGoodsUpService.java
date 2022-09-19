package com.atguigu.gmall.seckill.schedule;


import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SeckillGoodsUpService {


    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;

    @Autowired
    StringRedisTemplate redisTemplate;
    /**
     * 每天晚上2：00 上架 当天需要参与秒杀的所有商品
     */
    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void upSeckillGoods(){
        log.info("正在上架秒杀商品...");
        //查询当天需要参与秒杀的所有商品
//        String date = DateUtil.formatDate(new Date());
        //1、拿到当天需要参与秒杀的所有商品
        List<SeckillGoods> list = seckillGoodsService.getCurrentDaySeckillGoodsList();

        //2、redis缓存商品 + 本地缓存商品 + redis缓存库存量
        cacheOpsService.upSeckillGoods(list);
    }


    @Scheduled(cron = "0 0 1 * * ?")
    public void currentDaySeckillEnd(){
        //1、清缓存
        cacheOpsService.clearCache();
    }

}
