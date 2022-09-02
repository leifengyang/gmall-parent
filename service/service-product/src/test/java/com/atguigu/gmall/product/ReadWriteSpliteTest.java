package com.atguigu.gmall.product;


import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReadWriteSpliteTest {


    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Test
    public void testw(){
        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(4L);
        System.out.println(baseTrademark);


        baseTrademark.setTmName("小米");
        baseTrademarkMapper.updateById(baseTrademark);

        //改完后，再去查询，很可能查不到最新结果

        //让刚改完的下次查询强制走主库
        HintManager.getInstance().setWriteRouteOnly(); //强制走主库
        BaseTrademark baseTrademark2 = baseTrademarkMapper.selectById(4L);
        System.out.println("改完后查到的是："+baseTrademark2);


    }


    @Test
    public void testrw(){
        /**
         * 所有的负载均衡来到从库
         */
        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(4L);
        System.out.println(baseTrademark);


        BaseTrademark baseTrademark1 = baseTrademarkMapper.selectById(4L);
        System.out.println(baseTrademark1);


        BaseTrademark baseTrademark2 = baseTrademarkMapper.selectById(4L);
        System.out.println(baseTrademark2);

        BaseTrademark baseTrademark3 = baseTrademarkMapper.selectById(4L);
        System.out.println(baseTrademark3);
    }
}
