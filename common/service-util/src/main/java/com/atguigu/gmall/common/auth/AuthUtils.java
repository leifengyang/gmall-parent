package com.atguigu.gmall.common.auth;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class AuthUtils {

    /**
     * 利用Tomcat请求与线程绑定机制。+ Spring自己的 RequestContextHolder ThreadLocal原理
     *      = 同一个请求在处理期间，任何时候都能共享到数据
     * @return
     */
    public static UserAuthInfo getCurrentAuthInfo() {
        //1、拿到老请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        //2、获取信息
        UserAuthInfo authInfo = new UserAuthInfo();
        String header = request.getHeader(SysRedisConst.USERID_HEADER);
        if(!StringUtils.isEmpty(header)){
            authInfo.setUserId(Long.parseLong(header));
        }

        String tempHeader = request.getHeader(SysRedisConst.USERTEMPID_HEADER);
        authInfo.setUserTempId(tempHeader);



        return authInfo;
    }


}
