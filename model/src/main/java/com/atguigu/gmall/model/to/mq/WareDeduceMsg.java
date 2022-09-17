package com.atguigu.gmall.model.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class WareDeduceMsg {

    Long orderId;
    String consignee;
    String consigneeTel;
    String orderComment;
    String orderBody;
    String deliveryAddress;
    String paymentWay = "2";
    List<WareDeduceSkuInfo> details;

}
