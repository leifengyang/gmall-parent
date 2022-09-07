package com.atguigu.gmall.user.filter;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class HelloFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        //目标方法执行之前
        HttpServletResponse  resp = (HttpServletResponse) response;
        resp.sendRedirect("http://www.baidu.com");
        //放行
//        chain.doFilter(request,response);

        //目标方法执行之后

    }
}
