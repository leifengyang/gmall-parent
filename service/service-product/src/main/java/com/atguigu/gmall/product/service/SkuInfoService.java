package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfy
* @description 针对表【sku_info(库存单元表)】的数据库操作Service
* @createDate 2022-08-23 10:12:44
*/
public interface SkuInfoService extends IService<SkuInfo> {

    void saveSkuInfo(SkuInfo info);

    /**
     * 下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 上架
     * @param skuId
     */
    void onSale(Long skuId);
}
