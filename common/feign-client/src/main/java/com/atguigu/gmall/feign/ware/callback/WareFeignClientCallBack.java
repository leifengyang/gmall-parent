package com.atguigu.gmall.feign.ware.callback;

import com.atguigu.gmall.feign.ware.WareFeignClient;
import org.springframework.stereotype.Component;

@Component
public class WareFeignClientCallBack implements WareFeignClient {

    /**
     * 错误兜底
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public String hasStock(Long skuId, Integer num) {
        //统一显示有货
        return "1";
    }
}
