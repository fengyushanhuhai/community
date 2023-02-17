package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    // 定义切点 描述哪些方法哪些bean是要处理的目标
    // execution 关键字
    // 第一个* 代表方法 的返回值 * 代表任意返回值
    // com.nowcoder.community.Service 包名
    // .*.*(..) ==> 该包下所有的组件(类)的所有的方法的所有的参数
    // 从而匹配这个包下所有的组件所有的方法
    @Pointcut("execution(* com.nowcoder.community.Service.*.*(..))")
    public void pointcut(){

    }

    // 在连接点开头时记录日志
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }


    // 在连接点后面记录日志
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    // 有了返回值之后在处理
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    // 在抛异常时植入代码
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    // 既想在前面又想在后面植入逻辑
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {    // 连接点
        System.out.println("around before");
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }
}
