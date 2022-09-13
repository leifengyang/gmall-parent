package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.util.List;

/**
 * 订单提交 数据模型
 */
@Data
public class OrderSubmitVo {
    private String consignee;
    private String consigneeTel;
    private String deliveryAddress;
    private String paymentWay;
    private String orderComment;
    private List<CartInfoVo> orderDetailList;
}
