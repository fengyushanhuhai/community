package com.nowcoder.community;


import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	// 在测试类中可以启动配置类
public class mailTest {



    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMail(){
        mailClient.sendMail("1244200700@qq.com","test","合理了哦");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","sunday");       // thymeleaf模板引擎做的动态的值变量为username值为sunday

        String content = templateEngine.process("/mail/demo",context);   // 生成一个html
        System.out.println(content);
        mailClient.sendMail("1244200700@qq.com","html",content);
    }

}
