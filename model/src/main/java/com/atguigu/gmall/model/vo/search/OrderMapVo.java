package com.atguigu.gmall.model.vo.search;

import lombok.Data;

@Data
public class OrderMapVo {
    private String type; //排序类型， 1是综合，2是价格
    private String sort; //排序规则

}
