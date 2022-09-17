package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.util.List;

@Data
public class WareChildOrderVo {

    private Long orderId;//订单系统的订单ID（子订单的id）
    private String consignee;//	收货人
    private String consigneeTel;//	收货电话
    private String orderComment;//	订单备注
    private String orderBody;//	订单概要
    private String deliveryAddress;//	发货地址
    private String paymentWay;//	支付方式：  ‘1’ 为货到付款，‘2’为在线支付。
    private String wareId; //所在仓库id
    private List<WareChildOrderDetailItemVo> details; //当前子订单的所有商品


}
