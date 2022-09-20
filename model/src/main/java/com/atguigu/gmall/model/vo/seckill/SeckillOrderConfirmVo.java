package com.atguigu.gmall.model.vo.seckill;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SeckillOrderConfirmVo {

    private OrderInfo tempOrder; //redis的临时订单

    private Integer totalNum;
    private BigDecimal totalAmount;

    //用户收货地址列表
    private List<UserAddress> userAddressList;
}
