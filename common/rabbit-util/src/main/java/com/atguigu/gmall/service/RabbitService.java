package com.atguigu.gmall.service;


import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;



@Slf4j
@Service
public class RabbitService {


    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     *
     * @param maxNum    指定最大重试次数
     * @param uniqKey   指定识别消息的唯一key
     * @param messageTag 消息tag
     * @param channel    通道
     * @throws IOException
     */
    public void retryConsumMsg( Long maxNum, String uniqKey,Long messageTag, Channel channel) throws IOException {
        //lua脚本
        Long aLong = redisTemplate.opsForValue().increment(uniqKey);
        //重试消费10次
        if(aLong <= 10){
            channel.basicNack(messageTag,false,true);
        }else {
            channel.basicNack(messageTag,false,false); //不入队
            redisTemplate.delete(uniqKey);
            //记录到数据库，消费超了10次都未成功
            log.error("消息:{} , {} 次消费失败，",messageTag,maxNum);
        }
    }
}
