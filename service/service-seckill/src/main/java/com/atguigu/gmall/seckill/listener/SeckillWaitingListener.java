package com.atguigu.gmall.seckill.listener;


import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.SeckillTempOrderMsg;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;


@Slf4j
@Component
public class SeckillWaitingListener {


    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 监听等待扣减库存的秒杀订单
     * @param message
     * @param channel
     */
    @RabbitListener(
            bindings = {
                  @QueueBinding(
                          value= @Queue(value = MqConst.QUEUE_SECKILL_ORDERWAIT,durable = "true",autoDelete = "false",exclusive = "false"),
                          exchange = @Exchange(value = MqConst.EXCHANGE_SECKILL_EVENT,durable = "true",autoDelete = "false",type = "topic"),
                          key = MqConst.RK_SECKILL_ORDERWAIT
                  ),
            }
    )
    public void seckillWaiting(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        SeckillTempOrderMsg msg = Jsons.toObj(message, SeckillTempOrderMsg.class);
        log.info("监听到秒杀扣库存消息....{}",msg);
        Long skuId = msg.getSkuId();

        try {
            //数据库减库存失败就会报错
            seckillGoodsService.deduceSeckillGoods(skuId);
            //库存扣减成功
            rabbitTemplate.convertAndSend(MqConst.EXCHANGE_ORDER_EVNT,
                    MqConst.RK_ORDER_SECKILLOK,
                    Jsons.toStr(msg));
            //redis修改一下标志位
            String json = redisTemplate.opsForValue().get(SysRedisConst.SECKILL_ORDER + msg.getSkuCode());
            OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);
            //代表库存扣减操作过了
            orderInfo.setOperateTime(new Date());
            redisTemplate.opsForValue().set(SysRedisConst.SECKILL_ORDER + msg.getSkuCode(),Jsons.toStr(orderInfo));


            //给订单服务发送真正创建订单的消息
            channel.basicAck(tag,false);
        }catch (DataIntegrityViolationException e){
            log.error("扣库存失败：{}",e);
            //扣库存失败。redis临时单改成 x 错误标志。
            redisTemplate.opsForValue().set(SysRedisConst.SECKILL_ORDER+msg.getSkuCode(),"x");
            channel.basicAck(tag,false);

        }catch (Exception e){
            log.error("业务失败：{}",e);
            rabbitService.retryConsumMsg(10L,
                    SysRedisConst.MQ_RETRY+msg.getSkuCode(),tag,channel);
        }
    }
}
