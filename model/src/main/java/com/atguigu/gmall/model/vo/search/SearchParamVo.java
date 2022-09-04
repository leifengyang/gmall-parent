package com.atguigu.gmall.model.vo.search;

import lombok.Data;

/**
 * 封装检索条件
 */
@Data
public class SearchParamVo {
    Long category3Id;
    Long category1Id;
    Long category2Id;
    String keyword;
    String[] props;
    String trademark;
    String order;
    Integer pageNo;
}
