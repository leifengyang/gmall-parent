package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author lfy
* @description 针对表【base_attr_value(属性值表)】的数据库操作Service
* @createDate 2022-08-23 10:12:44
*/
public interface BaseAttrValueService extends IService<BaseAttrValue> {

    /**
     * 根据平台属性id查询出这个属性的所有属性值
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);
}
