package com.atguigu.gmall.order;


import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class OrderTest {

    @Autowired
    OrderInfoMapper orderInfoMapper;


    /**
     * 不带分片键的查询会全库全表都查询，然后结果归并
     */
    @Test
    public void testALl(){
        List<OrderInfo> infos = orderInfoMapper.selectList(null);
        for (OrderInfo info : infos) {
            System.out.println(info.getId()+"==>"+info.getTotalAmount()+"== user:"+info.getUserId());
        }
    }


    //9,223,372,036,854,775,808
    // 1亿: 100000000  能用：92,233,720,368 天 = 252,695,124 年
    // 自增：不同库会自增重复id
    //雪花id；也是Long这么长。64bit。第一高位不用。
    //剩下63位，前41bit 时间； 皮秒； 4096；
    // 2,199,023,255,552  1000000000000
    // 4Ghz 1s运行 4G下  8000000000 * 100

    //同一时刻：雪花算法最大多少不重复; 整个集群最多允许 400w多的不重复生成
    //同一时刻，同一个机器，最大 ：4,096


    @Test
    public void testQuery(){

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",2L);
        List<OrderInfo> infos = orderInfoMapper.selectList(wrapper);
        for (OrderInfo info : infos) {
            System.out.println(info.getTotalAmount());
        }

        System.out.println("=================");

        List<OrderInfo> infos2 = orderInfoMapper.selectList(wrapper);
        for (OrderInfo info : infos2) {
            System.out.println(info.getTotalAmount());
        }
    }


    @Test
    public void testSharding(){
        OrderInfo info = new OrderInfo();
        info.setTotalAmount(new BigDecimal("777"));
        info.setUserId(1L);
        orderInfoMapper.insert(info);


        System.out.println("1号用户订单插入完成....去 1库1表找");


        OrderInfo info2 = new OrderInfo();
        info2.setTotalAmount(new BigDecimal("666"));
        info2.setUserId(2L);
        orderInfoMapper.insert(info2);
        System.out.println("2号用户订单插入完成....去 0库2表找");

        //

    }


    @Test
    void hhh(){
        OrderInfo orderInfo = orderInfoMapper.selectById(205L);
        System.out.println(orderInfo);
    }
}
