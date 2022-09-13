package com.atguigu.gmall.order.biz;

import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;

/**
 * 订单业务
 */
public interface OrderBizService {

    /**
     * 获取订单确认页需要的数据
     * @return
     */
    OrderConfirmDataVo getConfirmData();

    /**
     * 生成交易流水号。
     * 1、追踪整个订单
     * 2、作为防重令牌
     * @return
     */
    String generateTradeNo();

    /**
     * 校验令牌
     */
    boolean checkTradeNo(String tradeNo);

    /**
     * 提交订单
     * @param submitVo
     * @return
     */
    Long submitOrder(OrderSubmitVo submitVo,String tradeNo);
}
