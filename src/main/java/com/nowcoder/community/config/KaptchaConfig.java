package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

// 工具配置类
@Configuration
public class KaptchaConfig {



    // 装配到spring容器中,从而的到一个Producer实例从而创建验证码和图片
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");  // 字体黑色
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");  // 验证码的取值范围
        properties.setProperty("kaptcha.textproducer.char.length","4");  // 验证码长度
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");  // 验证码上的干扰


        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
