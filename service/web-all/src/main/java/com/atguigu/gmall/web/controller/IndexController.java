package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.product.CategoryFeignClient;
import com.atguigu.gmall.model.to.CategoryTreeTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController //返回值会被服务器直接写给浏览器。 前后分离开发
//  浏览器。  ajax <==数据交互==> 服务器


// 浏览器。   search?xxx <====模板引擎找到页面地址,把页面数据写给浏览器==> 服务器
@Controller //返回值会被服务器认为是一个页面跳转地址，前后不分离开发
public class IndexController {


    @Autowired
    CategoryFeignClient categoryFeignClient;
    /**
     * 跳到首页
     * @return
     */
    @GetMapping({"/", "/index","/index.html"})
    public String indexPage(Model model) {


        //远程查询出所有菜单。封装成一个树形结构的模型
        Result<List<CategoryTreeTo>> result = categoryFeignClient
                .getAllCategoryWithTree();

        if(result.isOk()){
            //远程成功。 强类型语言
            List<CategoryTreeTo> data = result.getData();
            model.addAttribute("list",data);
        }
        //  classpath:/templates/index/index.html
        return "index/index";//页面的逻辑视图名
    }
}
