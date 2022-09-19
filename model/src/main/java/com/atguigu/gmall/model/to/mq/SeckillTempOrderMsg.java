package com.atguigu.gmall.model.to.mq;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SeckillTempOrderMsg {
    private Long userId;
    private Long skuId;
    private String skuCode;//秒杀码

}
