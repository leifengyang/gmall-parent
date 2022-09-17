package com.atguigu.gmall.order.listener;


import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.WareDeduceStatusMsg;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/**
 * 监听扣减库存结果
 */
@Slf4j
@Service
public class OrderStockDeduceListener {


    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    RabbitService rabbitService;
    /**
     * 如果标注的交换机、队列没有，会自动创建
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_WARE_ORDER,
                                    durable = "true",exclusive = "false",autoDelete = "false"),
                    exchange = @Exchange(name = MqConst.EXCHANGE_WARE_ORDER,
                                    durable = "true",autoDelete = "false",type = "direct"),
                    key = MqConst.RK_WARE_ORDER
            )
    })  //库存扣减结果监听
    public void stockDeduceListener(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        //按照库存扣减结果
        WareDeduceStatusMsg msg = Jsons.toObj(message, WareDeduceStatusMsg.class);
        Long orderId = msg.getOrderId();
        try {

            log.info("订单服务【修改订单出库状态】 监听到库存扣减结果：{}",msg);
            //1、改造业务：每次发消息的时候都带上
            //2、Sharding:不带分片键，查询可以（查所有库所有表）、增删改不行。

            //查询订单
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            ProcessStatus status = null;
            switch (msg.getStatus()){
                case "DEDUCTED": status = ProcessStatus.WAITING_DELEVER;break;
                case "OUT_OF_STOCK": status = ProcessStatus.STOCK_OVER_EXCEPTION;break;
                default: status = ProcessStatus.PAID;
            }

            orderInfoService.changeOrderStatus(orderId,
                    orderInfo.getUserId(),
                    status,
                    Arrays.asList(ProcessStatus.PAID)
            );
            channel.basicAck(tag,false);
        }catch (Exception e){
            String uk = SysRedisConst.MQ_RETRY + "stock:order:deduce:" + orderId;
            rabbitService.retryConsumMsg(10L,uk,tag,channel);
        }

    }
}
