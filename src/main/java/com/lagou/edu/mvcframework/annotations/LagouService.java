package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *@ClassName LagouService
 *@Description
 *@Author tuchao
 *@Date 2021/5/12 1:25
 *@Version 1.0
**/
@Documented//可以被javac文档编辑工具读取
@Target(ElementType.TYPE)//作用于类
@Retention(RetentionPolicy.RUNTIME)//生命周期，运行时，在内存中也生效
public @interface LagouService {
    String value() default "";
}
