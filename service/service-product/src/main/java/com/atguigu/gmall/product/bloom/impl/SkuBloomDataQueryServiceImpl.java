package com.atguigu.gmall.product.bloom.impl;

import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuBloomDataQueryServiceImpl implements BloomDataQueryService {

    @Autowired
    SkuInfoService skuInfoService;

    @Override
    public List queryData() {
        return skuInfoService.findAllSkuId();
    }
}
