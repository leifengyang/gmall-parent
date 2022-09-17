package com.atguigu.gmall.pay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.pay.config.AlipayProperties;
import com.atguigu.gmall.pay.service.AlipayService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    AlipayProperties alipayProperties;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public String getAlipayPageHtml(Long orderId) throws AlipayApiException {

        //3、拿到订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId).getData();
        if(orderInfo.getExpireTime().before(new Date())){
            throw new GmallException(ResultCodeEnum.ORDER_EXPIRED);
        }

        //1、创建一个 支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //2、构造支付请求要用的参数
        //浏览器跳到 ReturnUrl
        alipayRequest.setReturnUrl(alipayProperties.getReturnUrl());
        //支付宝给 NotifyUrl 发请求通知支付成功消息
        alipayRequest.setNotifyUrl(alipayProperties.getNotifyUrl());



        //4、构造支付数据
        Map<String,Object> bizContent = new HashMap<>();
        //设置订单对外交易号。  这个也是唯一识别订单。
        bizContent.put("out_trade_no",orderInfo.getOutTradeNo());
        bizContent.put("total_amount",orderInfo.getTotalAmount().toString());
        bizContent.put("subject","尚品汇订单-"+orderInfo.getOutTradeNo());
        bizContent.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContent.put("body",orderInfo.getTradeBody());
        //绝对超时
        String date = DateUtil.formatDate(orderInfo.getExpireTime(), "yyyy-MM-dd HH:mm:ss");
        //自动收单
        bizContent.put("time_expire",date);
        //....

        alipayRequest.setBizContent(Jsons.toStr(bizContent));


        //5、用支付宝客户端给支付宝发送支付请求。得到二维码收银台页面
        String result = alipayClient.pageExecute(alipayRequest).getBody();



        return result;
    }

    @Override
    public boolean rsaCheckV1(Map<String, String> paramMaps) throws AlipayApiException {
        boolean v1 = AlipaySignature
                .rsaCheckV1(paramMaps,
                        alipayProperties.getAlipayPublicKey(),
                        alipayProperties.getCharset(),
                        alipayProperties.getSignType());
        return v1;
    }

    @Override
    public void sendPayedMsg(Map<String, String> param) {

        //支付成功给 订单交换机发送一个消息；
        rabbitTemplate.convertAndSend(
                MqConst.EXCHANGE_ORDER_EVNT,
                MqConst.RK_ORDER_PAYED,
                Jsons.toStr(param));
    }
}
