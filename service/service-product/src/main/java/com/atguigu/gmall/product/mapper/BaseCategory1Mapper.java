package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 为了让操作数据库的Mapper组件放在容器中
 * 1、每个组件标注@Mapper注解
 * 2、@MapperScan("com.atguigu.gmall.product.mapper") //自动扫描这个包下的所有Mapper接口
 */
//@Mapper  //告诉springboot这是一个mapper组件，这个组件已经加入到Spring容器中了
public interface BaseCategory1Mapper extends BaseMapper<BaseCategory1> {

}
