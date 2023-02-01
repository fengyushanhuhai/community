package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;  // 用来发送邮件

    // 发送邮件的人 接收邮件的人 发送的标题和内容

    @Value("${spring.mail.username}")
    private String from;


    public void sendMail(String to, String subject, String content){
        try {
            // java 自带的mimeMessage以及mimeMessageHelp工具类
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);   // 设置发送人
            helper.setTo(to);       // 设置收件人
            helper.setSubject(subject);          // 设置主题
            helper.setText(content, true);  // 设置内容以及支持发送html文件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e){
            logger.error("发送邮件失败" + e.getMessage());
        }
    }
}
