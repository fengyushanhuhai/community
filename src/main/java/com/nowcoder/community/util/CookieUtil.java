package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request, String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }

        // 可以从request对象中获得cookie数组
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies){
                if(cookie.getName().equals(name)){  // 获取对应名字的cookie的值
                    return cookie.getValue();
                }
            }
        }
        // 如果遍历完都没有发现对应的cookie返回null
        return null;
    }
}
