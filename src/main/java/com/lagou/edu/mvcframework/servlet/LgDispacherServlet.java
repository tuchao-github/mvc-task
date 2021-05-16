package com.lagou.edu.mvcframework.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.lagou.demo.pojo.Headler;
import com.lagou.edu.mvcframework.annotations.LagouAutoWired;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.edu.mvcframework.annotations.LagouSecurity;
import com.lagou.edu.mvcframework.annotations.LagouService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName LgDispacherServlet.java
 * @Description TODO
 * @createTime 2021年05月12日 01:13:00
 */
public class LgDispacherServlet extends HttpServlet {

    private Properties properties = new Properties();
    /**缓存扫描到类的全限定类名*/
    private List<String> classNames = new ArrayList<>();
    /**ioc容器*/
    private Map<String,Object> ioc= new HashMap<>();
    /**处理器映射器*/
    // private Map<String,Method> headlerMapping= new HashMap<>();
    private List<Headler> headlerMapping = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件 springmvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);
        // 2. 扫描相关的类，扫描注解
        doScan(properties.getProperty("scanPackage"));
        // 3. 初始化bean对象（实现ioc容器）
        doInstance();
        // 4. 实现依赖注入
        doAutoWired();
        // 5. 构造一个HeadlerMapping（处理器映射器），将配置好的url与Method建立映射关系
        initHeadlerMappping();
        System.out.println("lagou mvc 初始化完成...");
        // 6. 等待请求进入，处理请求
    }

    /**
     * 构造一个HeadlerMapping（处理器映射器）
     * 最关键的环节
     * 目的：将url与method建立关联
     */
    private void initHeadlerMappping() {
        if(ioc.isEmpty()){return;}
        //遍历所有的对象
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取ioc中当前遍历对象的class类型
            Class<?> aClass = entry.getValue().getClass();
            //如果不包含Controller注解，则跳过
            if (!aClass.isAnnotationPresent(LagouController.class)){continue;}

            //获取类上的路径
            String baseUrl = "";
            if(aClass.isAnnotationPresent(LagouRequestMapping.class)){
                LagouRequestMapping annotation = aClass.getAnnotation(LagouRequestMapping.class);
                baseUrl = annotation.value();
            }


            String[] classUser = new String[0];
            //判断类上是否含有LagouSecurity的注解
            if(aClass.isAnnotationPresent(LagouSecurity.class)){
                LagouSecurity annotation = aClass.getAnnotation(LagouSecurity.class);
                //如果有则获取value里面的用户
                classUser = annotation.value();
            }

            // 获取类里面的所有方法进行遍历，判断是否包含RequestMapping注解，如果有则建立rul与method之间的映射关系
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                //如果没有，则跳过
                if(!method.isAnnotationPresent(LagouRequestMapping.class)){ continue; }
                //如果有，则处理
                LagouRequestMapping annotation = method.getAnnotation(LagouRequestMapping.class);
                String methodUrl = annotation.value();
                String url = baseUrl + methodUrl;

                Set<String> userNameSet = new HashSet<>();
                //将class里面的用户放进userNameSet
                userNameSet.addAll(Arrays.stream(classUser).collect(Collectors.toSet()));
                //判断方法上是否含有LagouSecurity的注解
                if(method.isAnnotationPresent(LagouSecurity.class)){
                    LagouSecurity methodAnnotation = method.getAnnotation(LagouSecurity.class);
                    //如果有则获取value里面的用户
                    String[] methodUser = methodAnnotation.value();
                    // 将method里面的用户放进userNameSet
                    userNameSet.addAll(Arrays.stream(methodUser).collect(Collectors.toSet()));
                }

                //把method所有信息封装成一个Headler
                Headler headler = new Headler(entry.getValue(), method, Pattern.compile(url),userNameSet);

                //计算参数的位置  //query(HttpServletRequest request, HttpServletResponse response,String name)
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    // 如果是request 和 response  对象,那么参数名称写成HttpServletRequest 和HttpServletResponse
                    if(parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class){
                        headler.getParamIndexMapping().put(parameter.getType().getSimpleName(),i);
                    }else{
                        headler.getParamIndexMapping().put(parameter.getName(),i);
                    }
                }

                // 建立url与method之间的映射关系（缓存起来）
                headlerMapping.add(headler);
            }
        }
    }

    /**实现依赖注入*/
    private void doAutoWired() {
        if(ioc.isEmpty()){return;}
        // 遍历ioc中的所有对象，查看对象中的字段，是否有@AutoWired注解，如果有则需要维护依赖关系
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取bean对象中的所有字段信息
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            // 遍历判断处理
            for (Field field : fields) {
                //@LagouAutoWired private IDemoService demoService
                if(!field.isAnnotationPresent(LagouAutoWired.class)){
                    continue;
                }

                // 有该注解
                LagouAutoWired annotation = field.getAnnotation(LagouAutoWired.class);
                //获取注解里面的id
                String beanName = annotation.value();
                if("".equals(beanName.trim())){
                    // 如果没有配置具体的bean id，则使用当前字段类型注入（接口注入）
                    beanName = field.getType().getName();
                }

                // 开启赋值，强制执行
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ioc容器
     * 基于classNames缓存的全限定类名，以及反射技术，完成对象的创建和管理
     */
    private void doInstance() {
        //不存在对象，则直接跳出
        if(classNames.size()==0){
            return;
        }
        try {
            //实例化每一个对象
            for (String className : classNames) {
                //反射  com.lagou.demo.controller.DemoController
                Class<?> aClass = Class.forName(className);
                //区分Controller，Service
                //判断是否包含LagouController注解
                if(aClass.isAnnotationPresent(LagouController.class)){
                    //Controller的id此处不做过多处理，就拿类的首字母小写作为id ，放进ioc容器中
                    //DemoController
                    String simpleName = aClass.getSimpleName();
                    //demoController
                    String lowerFristSimpleName = lowerFrist(simpleName);
                    //进行对象的实例化
                    Object o = aClass.newInstance();
                    //放进ioc容器进行管理
                    ioc.put(lowerFristSimpleName,o);
                }else if(aClass.isAnnotationPresent(LagouService.class)){
                    //获取注解
                    LagouService annotation = aClass.getAnnotation(LagouService.class);
                    //获取注解的value
                    String beanName = annotation.value();
                    //如果指定了id，以指定的id为准
                    if(!"".equals(beanName.trim())){
                        ioc.put(beanName,aClass.newInstance());
                    }else{
                        // 如果没有指定，就以类名首字母小写作为id
                        ioc.put(lowerFrist(beanName),aClass.newInstance());
                    }

                    // service层往往是有接口的，面向接口开发，此时使用接口名作为id，放入一份对象到ioc容器中，便于后期使用接口类型注入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        //以接口的全限定名作为id放入
                        ioc.put(anInterface.getName(),aClass.newInstance());
                    }
                }else{
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**首字母小写方法*/
    private String lowerFrist(String str){
        char[] chars = str.toCharArray();
        if('A'<= chars[0]&& chars[0] <='Z' ){
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    /**
     * 扫描类
     * scanPackage: com.lagou.demo --->磁盘的上面的文件夹（File） com/lagou/demo
     */
    private void doScan(String scanPackage) {
        //获取到扫描目录磁盘路径
        // 路径前面会带有/
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replace(".", "/");
        //文件路径存在中文，需要进行解码
        scanPackagePath = URLUtil.decode(scanPackagePath);
        File packFile = new File(scanPackagePath);
        //扫描目录获取所有类的全限定类名
        File[] files = packFile.listFiles();
        for (File file : files) {
            if(file.isDirectory()){
                //如果是目录，则为子package
                //递归
                //com.lagou.demo.controller
                doScan(scanPackage+"."+file.getName());
            }else if(file.getName().endsWith(".class")){
                // 如果是class文件，获取全限定类名放入缓存供后续进行实例化
                String className = scanPackage +"."+ file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**加载配置文件*/
    private void doLoadConfig(String contextConfigLocation) {
        //获取到配置文件流对象
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            //将流读取到properties对象
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 处理请求，根据url获取对应的Method方法，进行调用
        // // 获取uri
        // String requestURI = req.getRequestURI();
        // //获取到一个反射的方法
        // Method method = headlerMapping.get(requestURI);
        // //反射调用，需要传入对象，需要传入参数，此处无法完成调用，因为没有吧对象缓存起来，也没有参数！改造initHeadlerMapping()
        // method.invoke();

        // 根据uri获取到能够处理当前请求的Headler (从headlerMapping中(list))
        Headler headler = getHeadler(req);

        if (headler == null){
            resp.getWriter().write("404 not found");
            return;
        }

        // 根据req获取到当前请求的username参数,进行判断是否有权限访问当前headler
        //获取请求当中的username参数
        String userName = req.getParameter("username");
        // 如果userNameSet为空，或者userNameSet未匹配到当前请求用户，表示请求用户无权限
        if (headler.getUserNameSet() == null || !headler.getUserNameSet().contains(userName)) {
            resp.getWriter().write(userName + " No permission");
            return;
        }


        // 参数绑定
        // 获取所有参数类型数组，这个数组的长度就是最后要传入的args参数数组的长度
        Class<?>[] parameterTypes = headler.getMethod().getParameterTypes();
        // 根据上述数组长度创建一个新的数组（参数数组，传入反射调用的）
        Object[] paramValues = new Object[parameterTypes.length];
        // 以下为为了向参数中传值，而且得保证参数的顺序和方法中形参顺序一致
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 遍历req中的所有参数
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            //name=1&name=2 name[1,2]  修改为 name 1,2
            String value = StrUtil.join( ",",param.getValue());
            // 如果参数和方法中的参数匹配上了则填充参数
            if(!headler.getParamIndexMapping().containsKey(param.getKey())){continue;}
            // 方法形参中确实有此参数，则找到他的索引位置，对应的吧参数值放入paramValues
            Integer index = headler.getParamIndexMapping().get(param.getKey());
            // 把前台传递的参数放到对应的位置
            paramValues[index] = value;
        }

        Integer reqIndex = headler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName());
        paramValues[reqIndex] = req;

        Integer respIndex = headler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName());
        paramValues[respIndex] = resp;

        // 最终调用headler的method属性
        try {
            headler.getMethod().invoke(headler.getController(),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
    /**从headlerMapping中获取headler*/
    private Headler getHeadler(HttpServletRequest req) {
        if(headlerMapping.size()==0){return null;}
        String uri = req.getRequestURI();
        for (Headler headler : headlerMapping) {
            Matcher matcher = headler.getPattern().matcher(uri);
            if (!matcher.matches()){continue;}
            return headler;
        }
        return null;
    }
}
