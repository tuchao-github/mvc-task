package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName LagouRequestMapping.java
 * @Description TODO
 * @createTime 2021年05月12日 01:26:00
 */
@Documented//可以被javac文档编辑工具读取
@Target({ElementType.TYPE,ElementType.METHOD})//作用于类,方法
@Retention(RetentionPolicy.RUNTIME)//生命周期，运行时，在内存中也生效
public @interface LagouRequestMapping {
    String value() default "";
}
