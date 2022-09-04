package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {


    @Autowired
    SearchFeignClient searchFeignClient;
    /**
     * 检索列表页
     * 检索条件参数：
     * 1）、按照分类； category3Id=61、category2Id、category1Id=2
     * 2）、按照关键字； keyword=小米
     * 3）、按照属性；props=106:安卓手机:手机一级
     *             props=107:1200mAh到3000mAh:电池容量
     * 4）、按照品牌； trademark=1:小米
     * 5）、分页；  pageNo=1
     * 6）、排序；  order=1:desc
     *          1: 代表综合排序【热度分】
     *          2: 代表价格排序
     * vo: view object；专门封装页面数据，给页面交数据
     * @return
     */
    @GetMapping("/list.html")
    public String search(SearchParamVo searchParamVo, Model model, HttpServletRequest request){



        Result<SearchResponseVo> search = searchFeignClient.search(searchParamVo);
        SearchResponseVo data = search.getData();

        //把result数据展示到页面
        //1、以前检索页面点击传来的所有条件，原封不动返回给页面
        model.addAttribute("searchParam",data.getSearchParam());
        //2、品牌面包屑位置的显示
        model.addAttribute("trademarkParam",data.getTrademarkParam());
        //3、属性面包屑，是集合。集合里面每个元素是一个对象，拥有这些数据（attrName、attrValue、attrId）
        model.addAttribute("propsParamList",data.getPropsParamList());
        //4、所有品牌，是集合。集合里面每个元素是一个对象，拥有这些数据（tmId、tmLogoUrl、tmName）
        model.addAttribute("trademarkList",data.getTrademarkList());
        //5、所有属性，是集合。集合里面每个元素是一个对象，拥有这些数据（attrId，attrName，List<String> attrValueList， ）
        model.addAttribute("attrsList",data.getAttrsList());
        //6、排序信息。是对象。 拥有这些数据（type，sort）
        model.addAttribute("orderMap",data.getOrderMap());
        //7、所有商品列表。是集合。集合里面每个元素是一个对象,拥有这些数据(es中每个商品的详细数据)
        model.addAttribute("goodsList",data.getGoodsList());
        //8、分页信息
        model.addAttribute("pageNo",data.getPageNo());
        model.addAttribute("totalPages",data.getTotalPages());
        //9、url信息
        model.addAttribute("urlParam",data.getUrlParam());



        return "list/index";
    }
}
