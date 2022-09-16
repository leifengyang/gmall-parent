package com.atguigu.gmall.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(2012, "服务异常"),

    PAY_RUN(205, "支付中"),

    LOGIN_AUTH(2080, "未登陆"),
    LOGIN_ERROR(2081,"账号密码错误"),
    PERMISSION(209, "没有权限"),
    SECKILL_NO_START(210, "秒杀还没开始"),
    SECKILL_RUN(211, "正在排队中"),
    SECKILL_NO_PAY_ORDER(212, "您有未支付的订单"),
    SECKILL_FINISH(213, "已售罄"),
    SECKILL_END(214, "秒杀已结束"),
    SECKILL_SUCCESS(215, "抢单成功"),
    SECKILL_FAIL(216, "抢单失败"),
    SECKILL_ILLEGAL(217, "请求不合法"),
    SECKILL_ORDER_SUCCESS(218, "下单成功"),
    COUPON_GET(220, "优惠券已经领取"),
    COUPON_LIMIT_GET(221, "优惠券已发放完毕"),

    CART_OVERFLOW(3000,"购物车中商品总数超限，请移除部分商品，再添加"),
    CART_ITEM_SKUNUM_OVERFLOW(3001,"单个商品不允许超过200，请重新操作"),
    TOKEN_INVAILD(4000,"页面已过期，请重新刷新"),
    ORDER_NO_STOCK(4001,"订单商品库存不足："),
    ORDER_PRICE_CHANGED(4002,"订单中以下商品价格变化，请重新刷新再试；"),
    ORDER_EXPIRED(4003,"订单已过期，不能再进行支付");

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
