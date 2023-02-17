package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
// 工具类只用提供静态方法，不用注入
public class CommunityUtil {


    // 生成一个随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");    // 替换掉所有的横线为空
    }



    // MD5加密
    // hello --》 kjsdhfsf
    // hello + (salt随机字符串) --> kjsdhfsfsdfs
    public static String MD5(String key){
        if(StringUtils.isBlank(key)){   // 传入的字符为空直接返回null
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }


    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString(); // 返回的是json字符串
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",25);
        System.out.println(getJSONString(0, "ok", map));
    }

}
