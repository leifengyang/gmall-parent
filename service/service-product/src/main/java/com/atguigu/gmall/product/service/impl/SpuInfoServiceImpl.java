package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author lfy
* @description 针对表【spu_info(商品表)】的数据库操作Service实现
* @createDate 2022-08-23 10:12:44
*/
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo>
    implements SpuInfoService{

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageService spuImageService;

    @Autowired
    SpuSaleAttrService spuSaleAttrService;

    @Autowired
    SpuSaleAttrValueService spuSaleAttrValueService;


    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo info) {
        //1、把 spu基本信息保存到 spu_info表中
        spuInfoMapper.insert(info);
        Long spuId = info.getId(); //拿到spu保存后的自增id

        //2、把 spu的图片保存到 spu_image
        List<SpuImage> imageList = info.getSpuImageList();
        for (SpuImage image : imageList) {
            //回填spu_id
            image.setSpuId(spuId);
        }
        //批量保存图片
        spuImageService.saveBatch(imageList);


        //3、保存销售属性名 到 spu_sale_attr
        List<SpuSaleAttr> attrNameList = info.getSpuSaleAttrList();
        for (SpuSaleAttr attr : attrNameList) {
            //回填spuId
            attr.setSpuId(spuId);
            //4、拿到这个销售属性名对应的所有销售属性值集合
            List<SpuSaleAttrValue> valueList = attr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue value : valueList) {
                //回填spu_id
                value.setSpuId(spuId);
                String saleAttrName = attr.getSaleAttrName();
                //回填销售属性名
                value.setSaleAttrName(saleAttrName);
            }
            //保存销售属性值
            spuSaleAttrValueService.saveBatch(valueList);
        }
        //保存到数据库
        spuSaleAttrService.saveBatch(attrNameList);

    }
}




