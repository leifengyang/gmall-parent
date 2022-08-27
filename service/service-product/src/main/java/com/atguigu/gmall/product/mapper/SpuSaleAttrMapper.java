package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.ValueSkuJsonTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lfy
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Mapper
* @createDate 2022-08-23 10:12:44
* @Entity com.atguigu.gmall.product.domain.SpuSaleAttr
*/
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据spuId查询对应的所有销售属性名和值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSaleAttrAndValueBySpuId(@Param("spuId") Long spuId);

    /**
     * 查询当前sku对应的spu定义的所有销售属性名和值（固定好顺序）并且标记好当前sku属于哪一种组合
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSaleAttrAndValueMarkSku(@Param("spuId") Long spuId, @Param("skuId") Long skuId);

    /**
     * 查询所有sku的销售属性值组合可能
     * @param spuId
     * @return
     */
    List<ValueSkuJsonTo> getAllSkuValueJson(@Param("spuId") Long spuId);
}




