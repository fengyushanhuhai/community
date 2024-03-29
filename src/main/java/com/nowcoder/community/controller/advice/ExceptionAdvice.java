package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


// 这个注解只去扫描带有Controller的组件
// ControllerAdvice是对Controller的全局配置
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {


    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // 处理异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement stackTraceElement :e.getStackTrace()){
            logger.error(stackTraceElement.toString());
        }
        String xRequestedWith = request.getHeader("X-requested-with");
        // 判断异步请求
        if ("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }else{
            // 否则是普通请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }


}

