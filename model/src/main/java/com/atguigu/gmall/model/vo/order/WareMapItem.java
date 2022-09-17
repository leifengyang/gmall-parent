package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.util.List;

//[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
@Data
public class WareMapItem {
    private Long wareId;
    private List<Long> skuIds;
}
