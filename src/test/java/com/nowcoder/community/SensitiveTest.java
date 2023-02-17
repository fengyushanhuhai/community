package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	// 在测试类中可以启动配置类
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void  testSensitiveFilter(){
        String text = "这里可以☆赌☆博☆、可以☆嫖娼☆、可以☆吸毒☆、可以☆开票☆~ 哈哈哈~！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
