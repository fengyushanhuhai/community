package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class serviceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(serviceLogAspect.class);


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
    public void before(JoinPoint joinPoint){
        // 记录日志格式    用户{1.2.3.4}，在[xxx]访问了什么功能[com.nowcoder.community.Service.xxx()]
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        // 获取request对象
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // 获取ip
        String ip = request.getRemoteHost();
        // 获取时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 获取方法 类型名和方法名
       String target =  joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
       logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }
}
