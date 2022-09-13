package com.atguigu.gmall.feign.ware;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * url：指定请求发送的绝对路径。不会去注册中心寻找微服务的地址
 */
@FeignClient(value = "ware-manage",url = "${app.ware-url:http://localhost:9001/}")
public interface WareFeignClient {

//    //all?keyword=哈哈
//    @GetMapping(value = "/all",produces = "text/html;charset=utf-8")
//    String search(@RequestParam("keyword") String keyword);

    /**
     * 查询一个商品是否有库存
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/hasStock")
    String hasStock(@RequestParam("skuId") Long skuId,
                    @RequestParam("num") Integer num);


}
