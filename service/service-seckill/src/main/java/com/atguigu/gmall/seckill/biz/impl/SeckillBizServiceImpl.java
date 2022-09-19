package com.atguigu.gmall.seckill.biz.impl;
import java.math.BigDecimal;

import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.to.mq.SeckillTempOrderMsg;
import com.google.common.collect.Lists;
import com.atguigu.gmall.model.activity.CouponInfo;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillBizService;
import com.atguigu.gmall.seckill.service.SeckillGoodsCacheOpsService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillBizServiceImpl implements SeckillBizService {


    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public String generateSeckillCode(Long skuId) {
        //前置校验；

        //1、获取当前商品
        SeckillGoods goods = cacheOpsService.getSeckillGoodsDetail(skuId);

        if (goods == null) {
            //请求非法，当前商品不是参与秒杀的商品
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }

        //2、看这个商品是否可以开始秒杀了
        Date date = new Date();
        if (!date.after(goods.getStartTime())) {
            //秒杀还没开始，或者已经结束
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }
        if (!date.before(goods.getEndTime())) {
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }


        //3、判断是否还有足够库存. 每个请求放过去，内存库存自动减一
        if (goods.getStockCount() <= 0L) {
            //秒杀商品已经没有了
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }


        //往下放行；生成一个秒杀码。 固定算法。 同一个用户 + 同一天 + 同一个商品 = 唯一码（MD5）；

        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String c_date = DateUtil.formatDate(new Date());
        String code = generateCode(userId, skuId, c_date);

        return code;
    }

    @Override
    public boolean checkSeckillCode(Long skuId, String code) {
        //系统根据算法再生成一下
        String syscode = MD5.encrypt(AuthUtils.getCurrentAuthInfo().getUserId() + "_" + skuId + "_" + DateUtil.formatDate(new Date()));
        if(syscode.equals(code) && redisTemplate.hasKey(SysRedisConst.SECKILL_CODE+code)){
            return true;
        }
        return false;
    }


    @Override
    public ResultCodeEnum seckillOrder(Long skuId, String skuIdStr) {

        //0、检查秒杀码
        boolean b = checkSeckillCode(skuId,skuIdStr);
        if(!b){
            return ResultCodeEnum.SECKILL_ILLEGAL;
        }

        //1、获取当前商品
        SeckillGoods detail = cacheOpsService.getSeckillGoodsDetail(skuId);

        if (detail == null) {
            return ResultCodeEnum.SECKILL_ILLEGAL;
        }


        //2、看秒杀时间是否到了
        Date date = new Date();
        if (!date.after(detail.getStartTime())) {
            //秒杀还没开始，或者已经结束
            return ResultCodeEnum.SECKILL_NO_START;
        }
        if (!date.before(detail.getEndTime())) {
            return ResultCodeEnum.SECKILL_END;
        }

        //3、秒杀库存
        if (detail.getStockCount() <= 0L) {
            //秒杀商品已经没有了
            return ResultCodeEnum.SECKILL_FINISH;
        }
        //内存状态位-1
        detail.setStockCount(detail.getStockCount() - 1);



        //5、判断这个请求是否已经发送过了。 seckill:code
        Long increment = redisTemplate.opsForValue().increment(SysRedisConst.SECKILL_CODE + skuIdStr);
        if(increment > 2){
            //你已经发过一次请求了；
            return ResultCodeEnum.SUCCESS;
        }


        //4、真正开始秒杀
        //4.1）、先让redis预扣库存  seckill:goods:stock:49
        Long decrement = redisTemplate.opsForValue().decrement(SysRedisConst.CACHE_SECKILL_GOODS_STOCK + skuId);
        if(decrement >= 0){
            //说明redis扣减库存成功，发个消息，数据库自己慢慢创建秒杀订单去
            //4.2）、再让数据库真正去下秒杀单，去扣减；
            OrderInfo orderInfo = prepareTempSeckillOrder(skuId);
            redisTemplate.opsForValue().set(SysRedisConst.SECKILL_ORDER + skuIdStr, Jsons.toStr(orderInfo),1,TimeUnit.DAYS);
            //真正扣库存，创订单....
            String toStr = Jsons.toStr(new SeckillTempOrderMsg(orderInfo.getUserId(), skuId, skuIdStr));
            rabbitTemplate.convertAndSend(MqConst.EXCHANGE_SECKILL_EVENT,
                    MqConst.RK_SECKILL_ORDERWAIT,
                    toStr
                    );
            return ResultCodeEnum.SUCCESS;
        }else {
            return ResultCodeEnum.SECKILL_FINISH;
        }


    }

    @Override
    public ResultCodeEnum checkSeckillOrderStatus(Long skuId) {

        //TODO 订单状态检查

        return ResultCodeEnum.SECKILL_FINISH;
    }

    private OrderInfo prepareTempSeckillOrder(Long skuId) {
        SeckillGoods detail = cacheOpsService.getSeckillGoodsDetail(skuId);
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTotalAmount(detail.getCostPrice());
        orderInfo.setUserId(userId);
        orderInfo.setTradeBody(detail.getSkuName());
        orderInfo.setImgUrl(detail.getSkuDefaultImg());

        //订单详情：
        OrderDetail item = new OrderDetail();
        item.setSkuId(skuId);
        item.setUserId(userId);
        item.setSkuName(detail.getSkuName());
        item.setImgUrl(detail.getSkuDefaultImg());
        item.setOrderPrice(detail.getPrice());
        item.setSkuNum(1);
        item.setHasStock("1");
        item.setSplitTotalAmount(detail.getCostPrice());
        item.setSplitCouponAmount(detail.getPrice().subtract(detail.getCostPrice()));

        List<OrderDetail> orderDetailList = Arrays.asList(item);
        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }


    //
    private String generateCode(Long userId, Long skuId, String day) {
        //1、生成码
        String code = MD5.encrypt(userId + "_" + skuId + "_" + day);

        //2、redis存一份
        redisTemplate.opsForValue()
                .setIfAbsent(SysRedisConst.SECKILL_CODE + code,
                        "1", 1, TimeUnit.DAYS);


        return code;
    }
}
