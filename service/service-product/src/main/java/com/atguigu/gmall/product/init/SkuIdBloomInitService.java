package com.atguigu.gmall.product.init;


import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 容器启动成功以后，连上数据库，查到所有商品id。在布隆里面进行占位
 */
@Slf4j
@Service
public class SkuIdBloomInitService {

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    RedissonClient redissonClient;


    //TODO 布隆只能增，不能删除商品，如果真的数据库删除了商品，布隆怎么办？
    //      定期布隆重建。
    //【重建】：按钮触发

    /**
     * 项目一启动就运行
     */
    @PostConstruct  //当前组件对象创建成功以后
    public void initSkuBloom(){
        log.info("布隆初始化正在进行....");
        //1、查询出所有的skuId
        List<Long> skuIds  = skuInfoService.findAllSkuId();

        //2、把所有的id初始化到布隆过滤器中
        RBloomFilter<Object> filter =
                redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);

        //3、初始化布隆过滤器
        //long expectedInsertions, 期望插入的数据量
        //double falseProbability  误判率
        boolean exists = filter.isExists();
        if(!exists){
            //尝试初始化。如果布隆过滤器没有初始化过，就尝试初始化
            filter.tryInit(5000000,0.00001);
            //
        }


        //4、把所有的商品添加到布隆中。 不害怕某个微服务把这个事情做失败
        for (Long skuId : skuIds) {
            filter.add(skuId);
        }


        log.info("布隆初始化完成....，总计添加了 {} 条数据",skuIds.size());
        //
//        filter.contains(10L);

    }
}
