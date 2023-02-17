package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if (ticket != null){
            // 查询整个LoginTicket对象
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 判断凭证是否有效，有效才可以暂存后面使用
            // 凭证不为空并且状态为0并且超时时间必须晚于当前时间才是有效的凭证
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户(考虑多线程，线程隔离,暂存到一个线程中)
                // 并且这个user在模板引擎调用之前必须传到model中 ==> 重新postHandle方法
                hostHolder.setUser(user);
            }
        }
        return true;        // 表示继续往下执行
    }


    // controller调用之后模板调用之前进行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
        // 将hostHolder中的user清除是在模板引擎完成之后
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear(); // 将数据清理掉
    }
}
