package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.user.service.UserAddressService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;

/**
* @author lfy
* @description 针对表【user_address(用户地址表)】的数据库操作Service实现
* @createDate 2022-09-06 15:51:47
*/
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
    implements UserAddressService{

}




