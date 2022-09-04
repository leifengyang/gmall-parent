package com.atguigu.gmall.model.vo.search;

import com.atguigu.gmall.model.list.Goods;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import lombok.Data;

import java.util.List;

/**
 * 给检索页响应的所有数据
 */
@Data
public class SearchResponseVo {
    //检索用的所有参数
    private SearchParamVo searchParam;
    //品牌的面包屑
    private String trademarkParam;
    //平台属性面包屑
    private List<SearchAttr> propsParamList;
    //以上面包屑功能ok=====

    //所有品牌列表
    private List<TrademarkVo> trademarkList;
    //所有属性列表
    private List<AttrVo> attrsList;
    //以上筛选列表功能ok====


    //排序信息
    private OrderMapVo orderMap;
    //检索到的商品集合
    private List<Goods> goodsList;

    //当前页
    private Integer pageNo;
    //总页码
    private Integer totalPages;
    //url参数整个连接
    private String urlParam;

}
