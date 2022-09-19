package com.atguigu.gmall.seckill.biz;

import com.atguigu.gmall.common.result.ResultCodeEnum;

public interface SeckillBizService {

    /**
     * 生成秒杀码
     * @param skuId
     * @return
     */
    String generateSeckillCode(Long skuId);

    /**
     * 校验秒杀码
     * @param skuId
     * @param code
     * @return
     */
    boolean checkSeckillCode(Long skuId,String code);

    /**
     * 秒杀下单
     * @param skuId
     * @param skuIdStr
     */
    ResultCodeEnum seckillOrder(Long skuId, String skuIdStr);


    /**
     * 检查秒杀单状态
     * @param skuId
     * @return
     */
    ResultCodeEnum checkSeckillOrderStatus(Long skuId);
}
