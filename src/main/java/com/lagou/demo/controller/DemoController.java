package com.lagou.demo.controller;

import com.lagou.edu.mvcframework.annotations.LagouAutoWired;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName DemoController.java
 * @Description TODO
 * @createTime 2021年05月12日 02:24:00
 */
@LagouController
@LagouRequestMapping("/demo")
public class DemoController {

    @LagouAutoWired
    private IDemoService demoService;

    /**
     * /demo/query
     * @param request
     * @param response
     * @param name
     * @return
     */
    @LagouRequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response,String name){
        return demoService.get(name);
    }

}
