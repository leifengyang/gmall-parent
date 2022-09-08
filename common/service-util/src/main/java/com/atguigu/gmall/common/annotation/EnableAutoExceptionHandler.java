package com.atguigu.gmall.common.annotation;


import com.atguigu.gmall.common.config.threadpool.AppThreadPoolAutoConfiguration;
import com.atguigu.gmall.common.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(GlobalExceptionHandler.class)
public @interface EnableAutoExceptionHandler {
}
