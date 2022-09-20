package com.atguigu.gmall.seckill.biz.impl;
import java.math.BigDecimal;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.to.mq.SeckillTempOrderMsg;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.seckill.SeckillOrderConfirmVo;
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
import org.springframework.util.StringUtils;

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
            //内存状态位-1
            detail.setStockCount(detail.getStockCount() - 1);
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
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String code = MD5.encrypt(userId + "_" + skuId + "_" + DateUtil.formatDate(new Date()));
        //订单状态检查

        String json = redisTemplate.opsForValue().get(SysRedisConst.SECKILL_ORDER + code);
        if(json == null){
            return ResultCodeEnum.SECKILL_RUN;
        }
        if("x".equals(json)){
            return ResultCodeEnum.SECKILL_FINISH;
        }

        OrderInfo info = Jsons.toObj(json, OrderInfo.class);

        //1、是否已经下过单.
        if(info.getId()!=null && info.getId() > 0){
            return ResultCodeEnum.SECKILL_ORDER_SUCCESS;
        }

        //2、是否是抢单成功
        if(info.getOperateTime() != null){
            return ResultCodeEnum.SECKILL_SUCCESS;
        }

        //只要是成功状态就会继续查询最终状态
        return ResultCodeEnum.SUCCESS;
    }


    @Autowired
    UserFeignClient userFeignClient;

    @Override
    public SeckillOrderConfirmVo getSeckillOrderConfirmVo(Long skuId) {
        SeckillOrderConfirmVo confirmVo = null;

        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String code = MD5.encrypt(userId + "_" + skuId + "_" + DateUtil.formatDate(new Date()));

        String json = redisTemplate.opsForValue().get(SysRedisConst.SECKILL_ORDER + code);
        if(!StringUtils.isEmpty(json) && !"x".equals(json)){ //x
            OrderInfo info = Jsons.toObj(json, OrderInfo.class);
            confirmVo = new SeckillOrderConfirmVo();

            confirmVo.setTempOrder(info);
            confirmVo.setTotalNum(info.getOrderDetailList().size());
            confirmVo.setTotalAmount(info.getTotalAmount());
            //用户的收货地址
            Result<List<UserAddress>> addressList = userFeignClient.getUserAddressList();
            confirmVo.setUserAddressList(addressList.getData());
        }

        return confirmVo;
    }


    @Autowired
    OrderFeignClient orderFeignClient;
    @Override
    public Long submitSeckillOrder(OrderInfo orderInfo) {
        OrderInfo dbOrder = prepareAndSaveOrderInfoForDb(orderInfo);


        return dbOrder.getId();
    }

    private OrderInfo prepareAndSaveOrderInfoForDb(OrderInfo orderInfo) {
        OrderInfo redisData = null;
        Long skuId = orderInfo.getOrderDetailList().get(0).getSkuId();
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String code = MD5.encrypt(userId + "_" + skuId + "_" + DateUtil.formatDate(new Date()));
        //1、从redis拿到临时单数据
        String json = redisTemplate.opsForValue().get(SysRedisConst.SECKILL_ORDER + code);
        if(!StringUtils.isEmpty(json) && !"x".equals(json)){
            //2、获取临时单数据
            redisData = Jsons.toObj(json, OrderInfo.class);
            redisData.setConsignee(orderInfo.getConsignee());
            redisData.setConsigneeTel(orderInfo.getConsigneeTel());
            redisData.setOrderStatus(ProcessStatus.UNPAID.getOrderStatus().name());

            redisData.setDeliveryAddress(orderInfo.getDeliveryAddress());
            redisData.setOrderComment(orderInfo.getOrderComment());

            redisData.setOutTradeNo(System.currentTimeMillis() + "_" +userId);

            redisData.setCreateTime(new Date());
            Date date = new Date(System.currentTimeMillis() + 1000 * 60 * 15L);
            redisData.setExpireTime(date);
            redisData.setProcessStatus(ProcessStatus.UNPAID.name());

            //订单明细表

            redisData.setActivityReduceAmount(new BigDecimal("0"));
            redisData.setCouponAmount(new BigDecimal("0"));
            redisData.setOriginalTotalAmount(new BigDecimal("0"));
            redisData.setRefundableTime(new Date());
            redisData.setFeightFee(new BigDecimal("0"));
            redisData.setOperateTime(new Date());

            //远程保存订单
            Result<Long> result = orderFeignClient.submitSeckillOrder(redisData);
            redisData.setId(result.getData());
            //更新到redis
            redisTemplate.opsForValue().set(
                    SysRedisConst.SECKILL_ORDER+code,
                    Jsons.toStr(redisData));
        }

        return redisData;
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
