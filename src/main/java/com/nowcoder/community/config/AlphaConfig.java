package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration  // 标识为配置类
public class AlphaConfig {

    // 装配一个bean
    // 这个方法返回的对象将被装配到spring容器中，这个方法名就是bean的名字
    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat();
    }
}
