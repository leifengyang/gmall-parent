package com.atguigu.gmall.order.biz.impl;

import java.util.Date;

import com.atguigu.gmall.model.activity.CouponInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.SkuProductFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.OrderMsg;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.order.*;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

import com.atguigu.gmall.order.biz.OrderBizService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderBizServiceImpl implements OrderBizService {

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    SkuProductFeignClient skuProductFeignClient;

    @Autowired
    WareFeignClient wareFeignClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    OrderDetailService orderDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public OrderConfirmDataVo getConfirmData() {
        OrderConfirmDataVo vo = new OrderConfirmDataVo();
        //1、获取购物车中选中的商品
        //是购物车服务返回的
        //购物车中的商品只代表在redis中存储的数据，并不能代表最新价格
        List<CartInfo> data = cartFeignClient.getChecked().getData();


        List<CartInfoVo> infoVos = data.stream()
                .map(cartInfo -> {
                    CartInfoVo infoVo = new CartInfoVo();
                    infoVo.setSkuId(cartInfo.getSkuId());
                    infoVo.setImgUrl(cartInfo.getImgUrl());
                    infoVo.setSkuName(cartInfo.getSkuName());
                    //只要用到价格，就需要再查一遍；只要单下了，以后价格就用订单说明的定死的价格
                    //实时查价
                    Result<BigDecimal> price = skuProductFeignClient.getSku1010Price(cartInfo.getSkuId());
                    infoVo.setOrderPrice(price.getData());
                    infoVo.setSkuNum(cartInfo.getSkuNum());

                    //查询商品库存  http://localhost:9001/hasStock?skuId=43&num=9997
                    //feign 声明式 HTTP 客户端
                    String stock = wareFeignClient.hasStock(cartInfo.getSkuId(), cartInfo.getSkuNum());
                    infoVo.setHasStock(stock);

                    return infoVo;

                }).collect(Collectors.toList());

        vo.setDetailArrayList(infoVos);

        //2、统计商品的总数量
        Integer totalNum = infoVos.stream().map(CartInfoVo::getSkuNum)
                .reduce((o1, o2) -> o1 + o2)
                .get();
        vo.setTotalNum(totalNum);

        //3、统计商品的总金额
        BigDecimal totalAmount = infoVos.stream()
                .map(item -> item.getOrderPrice().multiply(new BigDecimal(item.getSkuNum() + "")))
                .reduce((o1, o2) -> o1.add(o2)).get();
        vo.setTotalAmount(totalAmount);

        //4、获取用户收货地址列表
        Result<List<UserAddress>> addressList = userFeignClient.getUserAddressList();
        vo.setUserAddressList(addressList.getData());

        //5、生成一个追踪号
        //5.1、订单的唯一追踪号，对外交易号（和第三方交互）。
        //5.2、用来防重复提交。 做防重令牌
        String tradeNo = generateTradeNo();
        //令牌前端交一份
        vo.setTradeNo(tradeNo);


        return vo;
    }

    @Override
    public String generateTradeNo() {
        // 20179009903209_2  同一个用户同一毫秒只能下一个单
        long millis = System.currentTimeMillis();
        UserAuthInfo info = AuthUtils.getCurrentAuthInfo();
        String tradeNo = millis + "_" + info.getUserId();

        //令牌redis存一份。
        redisTemplate.opsForValue()
                .set(SysRedisConst.ORDER_TEMP_TOKEN + tradeNo, "1", 15, TimeUnit.MINUTES);

        return tradeNo;
    }

    /**
     * 校验令牌
     *
     * @param tradeNo
     * @return
     */
    @Override
    public boolean checkTradeNo(String tradeNo) {
        //1、先看有没有，如果有就是正确令牌, 1, 0 。脚本校验令牌
        String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";

        /**
         * RedisScript<T> script,
         * List<K> keys, Object... args
         */
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(lua, Long.class),
                Arrays.asList(SysRedisConst.ORDER_TEMP_TOKEN + tradeNo),
                new String[]{"1"});

        if (execute > 0) {
            //令牌正确，并且已经删除
            return true;
        }
//        String val = redisTemplate.opsForValue().get(SysRedisConst.ORDER_TEMP_TOKEN + tradeNo);
//        if(!StringUtils.isEmpty(val)){
//            //redis有这个令牌。校验成功
//            redisTemplate.delete(SysRedisConst.ORDER_TEMP_TOKEN + tradeNo);
//            return true;
//        }

        return false;
    }

    @Override
    public Long submitOrder(OrderSubmitVo submitVo, String tradeNo) {
        //1、验令牌
        boolean checkTradeNo = checkTradeNo(tradeNo);
        if (!checkTradeNo) {
            throw new GmallException(ResultCodeEnum.TOKEN_INVAILD);
        }

        //2、验库存
        List<String> noStockSkus = new ArrayList<>();
        for (CartInfoVo infoVo : submitVo.getOrderDetailList()) {
            Long skuId = infoVo.getSkuId();
            String stock = wareFeignClient.hasStock(skuId, infoVo.getSkuNum());
            if (!"1".equals(stock)) {
                noStockSkus.add(infoVo.getSkuName());
            }
        }

        if (noStockSkus.size() > 0) {
            GmallException exception = new GmallException(ResultCodeEnum.ORDER_NO_STOCK);
            String skuNames = noStockSkus.stream()
                    .reduce((s1, s2) -> s1 + " " + s2)
                    .get();

            throw new GmallException(
                    ResultCodeEnum.ORDER_NO_STOCK.getMessage() + skuNames,
                    ResultCodeEnum.ORDER_NO_STOCK.getCode());
        }

        //3、验价格
        List<String> skuNames = new ArrayList<>();
        for (CartInfoVo infoVo : submitVo.getOrderDetailList()) {
            //1.00
            //1.0000001
            Result<BigDecimal> price = skuProductFeignClient.getSku1010Price(infoVo.getSkuId());
//            BigDecimal decimal = price.getData().subtract(infoVo.getOrderPrice());
//            if( decimal.doubleValue() < 0.0001){
//                //认为对等
//            }
            if (!price.getData().equals(infoVo.getOrderPrice())) {
                skuNames.add(infoVo.getSkuName());
            }
        }
        if (skuNames.size() > 0) {
            String skuName = skuNames.stream()
                    .reduce((s1, s2) -> s1 + " " + s2)
                    .get();
            //有价格发生变化的商品
            throw new GmallException(
                    ResultCodeEnum.ORDER_PRICE_CHANGED.getMessage() + "<br/>" + skuName,
                    ResultCodeEnum.ORDER_PRICE_CHANGED.getCode());
        }


        //4、把订单信息保存到数据库  orderId userId
        Long orderId = orderInfoService.saveOrder(submitVo, tradeNo);


        //5、清除购物车中选中的商品
        cartFeignClient.deleteChecked();

        //45min不支付就要关闭。
        //给MQ发一个消息。说明这个订单创建成功了。
        //只要关单失败，消费者下次启动消息还在

        return orderId;
    }

    @Override
    public void closeOrder(Long orderId, Long userId) {
        ProcessStatus closed = ProcessStatus.CLOSED;
        List<ProcessStatus> expected = Arrays.asList(ProcessStatus.UNPAID, ProcessStatus.FINISHED);
        //如果是未支付或者已结束才可以关闭订单 CAS
        orderInfoService.changeOrderStatus(orderId, userId, closed, expected);
        //process_status，order_status

        //update order_info set
        //  process_status=CLOSED,order_status=CLOSED
        // where user_id=userId and order_id=orderId and order_status IN (UNPAID,FINISHED)


    }

    @Override
    public List<WareChildOrderVo> orderSplit(OrderWareMapVo params) {
        //1、父订单id
        Long orderId = params.getOrderId();
        //1.1、查询父单
        OrderInfo parentOrder = orderInfoService.getById(orderId);
        //1.2、查询父单明细
        List<OrderDetail> details = orderDetailService.getOrderDetails(orderId, parentOrder.getUserId());
        parentOrder.setOrderDetailList(details);
        //==========父订单完整信息准备完成=================


        //2、库存的组合
        List<WareMapItem> items = Jsons.toObj(params.getWareSkuMap(), new TypeReference<List<WareMapItem>>() {
        });

        //3、=========== 开始拆分 ============
        List<OrderInfo> spiltOrders = items.stream()
                .map(wareMapItem -> {
                    //4、保存子订单
                    OrderInfo orderInfo = saveChildOrderInfo(wareMapItem, parentOrder);
                    return orderInfo;
                }).collect(Collectors.toList());

        //把父单状态修改为 已拆分
        orderInfoService.changeOrderStatus(parentOrder.getId(),
                parentOrder.getUserId(),
                ProcessStatus.SPLIT,
                Arrays.asList(ProcessStatus.PAID)
                );

        //4、转换为库存系统需要的数据
        return convertSpiltOrdersToWareChildOrderVo(spiltOrders);
    }

    private List<WareChildOrderVo> convertSpiltOrdersToWareChildOrderVo(List<OrderInfo> spiltOrders) {
        List<WareChildOrderVo> orderVos = spiltOrders.stream().map(orderInfo -> {
            WareChildOrderVo orderVo = new WareChildOrderVo();
            //封装:
            orderVo.setOrderId(orderInfo.getId());
            orderVo.setConsignee(orderInfo.getConsignee());
            orderVo.setConsigneeTel(orderInfo.getConsigneeTel());
            orderVo.setOrderComment(orderInfo.getOrderComment());
            orderVo.setOrderBody(orderInfo.getTradeBody());
            orderVo.setDeliveryAddress(orderInfo.getDeliveryAddress());
            orderVo.setPaymentWay(orderInfo.getPaymentWay());
            orderVo.setWareId(orderInfo.getWareId());

            //子订单明细 List<WareChildOrderDetailItemVo>  List<OrderDetail>
            List<WareChildOrderDetailItemVo> itemVos = orderInfo.getOrderDetailList()
                    .stream()
                    .map(orderDetail -> {
                        WareChildOrderDetailItemVo itemVo = new WareChildOrderDetailItemVo();
                        itemVo.setSkuId(orderDetail.getSkuId());
                        itemVo.setSkuNum(orderDetail.getSkuNum());
                        itemVo.setSkuName(orderDetail.getSkuName());
                        return itemVo;
                    }).collect(Collectors.toList());
            orderVo.setDetails(itemVos);
            return orderVo;
        }).collect(Collectors.toList());
        return orderVos;
    }


    //保存一个子订单
    private OrderInfo saveChildOrderInfo(WareMapItem wareMapItem, OrderInfo parentOrder) {
        //1、子订单中的所有商品  49,40,51
        List<Long> skuIds = wareMapItem.getSkuIds();
        //2、子订单是在哪个仓库出库的
        Long wareId = wareMapItem.getWareId();


        //3、子订单
        OrderInfo childOrderInfo = new OrderInfo();
        childOrderInfo.setConsignee(parentOrder.getConsignee());
        childOrderInfo.setConsigneeTel(parentOrder.getConsigneeTel());

        //4、获取到子订单的明细
        List<OrderDetail> childOrderDetails = parentOrder.getOrderDetailList()
                .stream()
                .filter(orderDetail -> skuIds.contains(orderDetail.getSkuId()))
                .collect(Collectors.toList());

        //流式计算
        BigDecimal decimal = childOrderDetails.stream()
                .map(orderDetail ->
                        orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum() + "")))
                .reduce((o1, o2) -> o1.add(o2))
                .get();
        //当前子订单负责所有明细的总价
        childOrderInfo.setTotalAmount(decimal);


        childOrderInfo.setOrderStatus(parentOrder.getOrderStatus());
        childOrderInfo.setUserId(parentOrder.getUserId());
        childOrderInfo.setPaymentWay(parentOrder.getPaymentWay());
        childOrderInfo.setDeliveryAddress(parentOrder.getDeliveryAddress());
        childOrderInfo.setOrderComment(parentOrder.getOrderComment());
        //对外流水号
        childOrderInfo.setOutTradeNo(parentOrder.getOutTradeNo());
        //子订单体
        childOrderInfo.setTradeBody(childOrderDetails.get(0).getSkuName());
        childOrderInfo.setCreateTime(new Date());
        childOrderInfo.setExpireTime(parentOrder.getExpireTime());
        childOrderInfo.setProcessStatus(parentOrder.getProcessStatus());


        //每个子订单未来发货以后这个都不一样
        childOrderInfo.setTrackingNo("");
        childOrderInfo.setParentOrderId(parentOrder.getId());
        childOrderInfo.setImgUrl(childOrderDetails.get(0).getImgUrl());

        //子订单的所有明细。也要保存到数据库
        childOrderInfo.setOrderDetailList(childOrderDetails);
        childOrderInfo.setWareId("" + wareId);
        childOrderInfo.setProvinceId(0L);
        childOrderInfo.setActivityReduceAmount(new BigDecimal("0"));
        childOrderInfo.setCouponAmount(new BigDecimal("0"));
        childOrderInfo.setOriginalTotalAmount(new BigDecimal("0"));

        //根据当前负责的商品决定退货时间
        childOrderInfo.setRefundableTime(parentOrder.getRefundableTime());

        childOrderInfo.setFeightFee(parentOrder.getFeightFee());
        childOrderInfo.setOperateTime(new Date());


        //保存子订单
        orderInfoService.save(childOrderInfo);

        //保存子订单的明细
        childOrderInfo.getOrderDetailList().stream().forEach(orderDetail -> orderDetail.setOrderId(childOrderInfo.getId()));

        List<OrderDetail> detailList = childOrderInfo.getOrderDetailList();
        //子单明细保存完成
        orderDetailService.saveBatch(detailList);


        return childOrderInfo;
    }

//    @Scheduled(cron = "0 */5 * * * ?")
//    public void closeOrder(Long orderId){
//
//    }

}
