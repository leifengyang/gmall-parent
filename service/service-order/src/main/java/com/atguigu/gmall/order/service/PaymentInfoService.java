package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author lfy
* @description 针对表【payment_info(支付信息表)】的数据库操作Service
* @createDate 2022-09-09 14:41:24
*/
public interface PaymentInfoService extends IService<PaymentInfo> {


    /**
     * 根据支付宝回调的数据保存一个支付流水信息。
     * @param map  支付宝返回的数据
     * @return 刚才保存的 PaymentInfo
     */
    PaymentInfo savePaymentInfo(Map<String, String> map);
}
