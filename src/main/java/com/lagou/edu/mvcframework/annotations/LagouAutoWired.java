package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName LagouAutoWired.java
 * @Description TODO
 * @createTime 2021年05月12日 01:27:00
 */
@Documented//可以被javac文档编辑工具读取
@Target(ElementType.FIELD)//作用于域
@Retention(RetentionPolicy.RUNTIME)//生命周期，运行时，在内存中也生效
public @interface LagouAutoWired {
    String value() default "";
}
