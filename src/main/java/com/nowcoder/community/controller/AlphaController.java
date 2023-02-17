package com.nowcoder.community.controller;

import com.nowcoder.community.Service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")  // 浏览器通过这个名字来访问这个类
public class AlphaController {

    // 处理浏览器请求的方法
    @RequestMapping("/hello")   // 该方法可以被浏览器访问的前提是也需要有这个注解浏览器才可以访问这个方法
    @ResponseBody               // 如果没有下面这个注解，返回的是网页，所以需要加这个注解进行声明
    public String sayHello(){
        return "Hello Spring Boot!";
    }



    @Autowired
    private AlphaService service;

    @RequestMapping("/go")
    @ResponseBody
    public String go(){
        return service.useDao();
    }

    // 请求和相应
    @RequestMapping("/http")
    // 返回值为void，原因在于通过@ResponseBody注解可以向浏览器输出任何数据
    // 常用的接口 HttpServletRequest请求对象  HttpServletResponse相应对象， 前端控制器会自动将这两个对象传给你
    public void http(HttpServletRequest request, HttpServletResponse response){
        // 处理请求 读取请求的数据
        // 请求行的第一行数据
        System.out.println(request.getMethod());    // 获取请求方式
        System.out.println(request.getServletPath());   // 获取请求路径

        // 请求的消息头 若干行数据
        Enumeration<String> enumeration =  request.getHeaderNames(); // 获取请求行
        while(enumeration.hasMoreElements()){
            String key = enumeration.nextElement(); // key 请求行的名字
            String value = request.getHeader(key);  // value 请求行的值
            System.out.println(key + ":" + value);
        }

        // 可以获得请求体，业务数据，参数
        System.out.println(request.getParameter("code"));   // 获取参数名为code的参数值

        // 返回相应数据(服务器向浏览器返回数据)
        // 首先设置返回数据的类型
        response.setContentType("text/html;charset=utf-8"); //返回网页类型并且支持中文
        // 获取response的输出流，并向浏览器返回数据
        try{
            PrintWriter writer = response.getWriter();
            writer.write("<h1>牛客网<h1>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 处理请求数据(简便方式)
    // GET请求（获取某些数据，默认发送的请求就是GET请求） 有两种传参的方式

    // 查询路径 /students？current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET) // 设置请求路径以及请求的方法
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false,defaultValue = "10") int limit){

        // @RequestParam注解的意思前端控制器通过判断浏览器传入的参数的名字是否和当前参数名一致，
        // 如果一致就将浏览器输入的参数的值传给当前参数
        // required = false 代表浏览器可以不传这个参数，所以通过defaultValue设置一个默认参数值
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String get(@PathVariable("id") int id){
        // @PathVariable注解 为路径变量
        // 当@RequestMapping中的路径参数中有{}包括(将参数拼到路径中)，那么可以通过@PathVariable("")，将值直接传给当前参数
        System.out.println(id);
        return "A Student";
    }

    // 浏览器向服务器提交数据 POST请求(带有表单的网页)
    // GET请求传参会带在路径上，明面上传;
    // 同时这个路径长度是有限的，当参数很多需要提交时选择POST
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public  String save(String name, int age){
        // 直接声明参数，当参数中的名字和表单中的参数名字一致就可以传过来,可以加但是不需要加@RequestParam注解
        System.out.println(name);
        System.out.println(age);
        return "success";
    }


    // 响应HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){   // 返回model和view两份数据
        // 1、实例化
        ModelAndView modelAndView = new ModelAndView();
        // 2、模板中需要多少个变量，add多少个数据
        modelAndView.addObject("name","张三");
        modelAndView.addObject("age",30);
        // 3、设置模板的路径和名字
        // 模板放在templates目录下，不用写/templates,直接写下级目录，并且由于thymeleaf模板引擎中.html不用写，view
        modelAndView.setViewName("/demo/view");
        // 4、返回
        return modelAndView;
    }

    // 第二种方式 更加简便
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model){   // model会自动实例化
        model.addAttribute("name","中国科学技术大学"); 
        model.addAttribute("age","70");
        return "/demo/view";    // 返回的是模板的路径
    }


    // 服务器向浏览器响应JSON数据，通常是异步请求当中
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody //不加返回html，加了才可以返回字符串
    public Map<String,Object> getEmp(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",18);
        map.put("salary",5000);
        return map;
    }


    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody //不加返回html，加了才可以返回字符串
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",18);
        map.put("salary",5000);
        list.add(map);
        map = new HashMap<>();
        map.put("name","张四");
        map.put("age",19);
        map.put("salary",5000);
        list.add(map);
        map = new HashMap<>();
        map.put("name","张五");
        map.put("age",20);
        map.put("salary",5000);
        list.add(map);
        return list;
    }

    // cookie相关实例
    // 浏览器请求，服务器发送cookie
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){  // 将cookie存到response中所以在响应时才能自动将数据传给浏览器
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());  // 每一个cookie只能存一对k-v
        // 设置生效的范围（不需要所有的路径都要发cookie）
        cookie.setPath("/community/alpha");
        // 设置cookie的生存时间(默认cookie存在浏览器里，关掉就消失；但是设置了生存时间就存在了硬盘里)
        cookie.setMaxAge(60 * 10);  // 单位为s

        // 发送cookie
        response.addCookie(cookie);
        return "set cookie";
    }

    // 浏览器携带cookie访问服务器
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }


    // session的使用实例
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        // 创建session(自动创建，声明就会被注入进来) session可以存任何类型的数据，cookie只能存字符串
        session.setAttribute("id",1);
        session.setAttribute("name","test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));


        return "get session";
    }


    // ajax 示例 异步请求返回的不是网页而是字符串
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功！");
    }


}
