package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lfy
 * @description 针对表【base_attr_info(属性表)】的数据库操作Service实现
 * @createDate 2022-08-23 10:12:44
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo>
        implements BaseAttrInfoService {


    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrInfoAndValueByCategoryId(Long c1Id, Long c2Id, Long c3Id) {

        //sql 查询指定分类下的所有属性名和值
        List<BaseAttrInfo> infos = baseAttrInfoMapper.getAttrInfoAndValueByCategoryId(c1Id, c2Id, c3Id);
        return infos;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo info) {


        if (info.getId() == null) {
            //1、进行属性新增操作
            addBaseAttrInfo(info);
        } else {
            //2、进行属性修改操作
            updateBaseAttrInfo(info);
        }


    }

    private void updateBaseAttrInfo(BaseAttrInfo info) {
        //2.1）、改属性名信息
        baseAttrInfoMapper.updateById(info);
        //2.2）、改属性值
        //以前的值：2019、2020、2021、2022
        //现在前端：2019以前、2021、2023以后
        //1、老记录全删，新提交全新增。导致引用失效
        //2、正确做法：
        List<BaseAttrValue> valueList = info.getAttrValueList();


        //先删除
        //1、前端提交来的所有属性值id
        List<Long> vids = new ArrayList<>();
        for (BaseAttrValue attrValue : valueList) {
            Long id = attrValue.getId();
            if (id != null) {
                vids.add(id);
            }
        }
        // delete * from base_attr_value where attr_id=11 and id not in(59,61)
        if(vids.size()>0){
            //部分删除
            QueryWrapper<BaseAttrValue> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("attr_id", info.getId());
            deleteWrapper.notIn("id", vids);
            baseAttrValueMapper.delete(deleteWrapper);
        }else {
            //全删除，前端一个属性值id都没带。把这个属性id下的所有属性值全部删除
            QueryWrapper<BaseAttrValue> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("attr_id", info.getId());
            baseAttrValueMapper.delete(deleteWrapper);
        }


        for (BaseAttrValue attrValue : valueList) {
            //修改属性值
            if (attrValue.getId() != null) {
                //属性值有id，说明数据库以前有，此次只需要修改即可
                baseAttrValueMapper.updateById(attrValue);
            }

            if (attrValue.getId() == null) {
                //说明数据库以前没有是新增
                attrValue.setAttrId(info.getId());
                baseAttrValueMapper.insert(attrValue);
            }

            //判断前端没提交的都是需要删除的？
            //数据库：59,60,61,62
            //现在： 59,61
            //前端没提交： 60,62 说明这两个要删除[差集]
        }
    }

    private void addBaseAttrInfo(BaseAttrInfo info) {
        //1、保存属性名
        baseAttrInfoMapper.insert(info);
        //拿到刚才保存好的属性名的自增id
        Long id = info.getId();

        //2、保存属性值
        List<BaseAttrValue> valueList = info.getAttrValueList();
        for (BaseAttrValue value : valueList) {
            //回填属性名记录的自增id
            value.setAttrId(id);
            baseAttrValueMapper.insert(value);
        }
    }
}




