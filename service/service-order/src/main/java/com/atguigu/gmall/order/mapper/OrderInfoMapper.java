package com.atguigu.gmall.order.mapper;


import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lfy
* @description 针对表【order_info(订单表 订单表)】的数据库操作Mapper
* @createDate 2022-09-09 14:41:24
* @Entity com.atguigu.gmall.order.domain.OrderInfo
*/
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    void updateOrderStatus(@Param("orderId") Long orderId,
                           @Param("userId") Long userId,
                           @Param("processStatus") String processStatus,
                           @Param("orderStatus") String orderStatus,
                           @Param("expects") List<String> expects);

}




