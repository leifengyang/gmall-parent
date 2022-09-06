package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.model.vo.user.LoginSuccessVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfy
* @description 针对表【user_info(用户表)】的数据库操作Service
* @createDate 2022-09-06 15:51:47
*/
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户登录
     * @return
     */
    LoginSuccessVo login(UserInfo info);

    /**
     * 用户退出
     * @param token
     */
    void logout(String token);
}
