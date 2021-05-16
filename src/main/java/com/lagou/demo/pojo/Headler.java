package com.lagou.demo.pojo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Headler.java
 * @Description TODO
 * @createTime 2021年05月12日 22:44:00
 */
public class Headler {
    /**method.invoke(obj,) 执行反射方法需要class对象*/
    private Object controller;
    private Method method;
    /**spring中url是支持正则的*/
    private Pattern pattern;
    /**参数顺序，是为了进行参数绑定，key是参数名，value代表是第几个参数<name,2>*/
    private Map<String,Integer> paramIndexMapping;
    /**用户名存储集合，用来绑定用户权限*/
    private Set<String> userNameSet;

    public Headler(Object controller, Method method, Pattern pattern,Set<String> userNameSet) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
        this.paramIndexMapping = new HashMap<>();
        this.userNameSet = userNameSet;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }

    public Set<String> getUserNameSet() {
        return userNameSet;
    }

    public void setUserNameSet(Set<String> userNameSet) {
        this.userNameSet = userNameSet;
    }
}
