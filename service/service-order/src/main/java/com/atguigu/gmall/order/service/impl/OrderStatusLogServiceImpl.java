package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.order.OrderStatusLog;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.order.service.OrderStatusLogService;
import com.atguigu.gmall.order.mapper.OrderStatusLogMapper;
import org.springframework.stereotype.Service;

/**
* @author lfy
* @description 针对表【order_status_log】的数据库操作Service实现
* @createDate 2022-09-09 14:41:24
*/
@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog>
    implements OrderStatusLogService{

}




