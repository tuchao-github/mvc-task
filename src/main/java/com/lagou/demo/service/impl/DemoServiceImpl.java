package com.lagou.demo.service.impl;

import com.lagou.edu.mvcframework.annotations.LagouService;
import com.lagou.demo.service.IDemoService;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName DemoServiceImpl.java
 * @Description TODO
 * @createTime 2021年05月12日 02:26:00
 */
@LagouService("demoService")
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        System.out.println("service 实现类中的请求参数：name="+name);
        return name;
    }
}
