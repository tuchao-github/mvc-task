package com.lagou.demo.controller;

import com.lagou.demo.service.IDemoService;
import com.lagou.edu.mvcframework.annotations.LagouAutoWired;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.edu.mvcframework.annotations.LagouSecurity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName DemoController.java
 * @Description TODO
 * @createTime 2021年05月12日 02:24:00
 */
@LagouSecurity({"zhangsan"})
@LagouController
@LagouRequestMapping("/classDemo")
public class ClassDemoController {

    @LagouAutoWired
    private IDemoService demoService;

    @LagouRequestMapping("/query01")
    public String query01(HttpServletRequest request, HttpServletResponse response,String username){
        return demoService.get(username);
    }

    @LagouSecurity({"lisi"})
    @LagouRequestMapping("/query02")
    public String query02(HttpServletRequest request, HttpServletResponse response,String username){
        return demoService.get(username);
    }

}
