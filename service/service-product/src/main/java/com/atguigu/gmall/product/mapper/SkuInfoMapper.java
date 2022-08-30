package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author lfy
* @description 针对表【sku_info(库存单元表)】的数据库操作Mapper
* @createDate 2022-08-23 10:12:44
* @Entity com.atguigu.gmall.product.domain.SkuInfo
*/
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 更新 sku的is_sale字段
     * @param skuId
     * @param sale
     */
    void updateIsSale(@Param("skuId") Long skuId,
                      @Param("sale") int sale);

    /**
     * 查询一个商品的实时价格
     * @param skuId
     * @return
     */
    BigDecimal getRealPrice(@Param("skuId") Long skuId);

    /**
     * 查询所有的skuId
     * @return
     */
    List<Long> getAllSkuId();

}




