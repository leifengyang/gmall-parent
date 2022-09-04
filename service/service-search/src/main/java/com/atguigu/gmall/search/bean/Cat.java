package com.atguigu.gmall.search.bean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Cat是一个工厂，
 *  1、会给容器中调用自己 getObject() 创建一个组件，
 *  2、组件类型就是 getObjectType 类型
 * 如果用Spring默认的IOC容器 BeanFactory，默认工厂，只造默认组件Bean，不做自定义Bean的创造逻辑
 *
 * FactoryBean可以自己定义制造组件的工厂；
 *
 */

@Component  //cat
public class Cat implements FactoryBean<Person> {
    @Override
    public Person getObject() throws Exception {
        return new Person();
    }

    @Override
    public Class<?> getObjectType() {
        return Person.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
