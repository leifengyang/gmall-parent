package com.atguigu.gmall.search;


import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.search.service.GoodsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SearchTest {

    @Autowired
    GoodsService goodsService;

    @Test
    void testSearch(){
        SearchParamVo vo = new SearchParamVo();
        vo.setCategory3Id(61L);
//        vo.setTrademark("4:小米");
//        vo.setProps(new String[]{"4:128GB:机身存储"});
//        vo.setOrder("2:asc");
//        vo.setKeyword("小米");
        goodsService.search(vo);
    }
}
