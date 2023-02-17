package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {


    private  static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegister(){
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if (map == null || map.isEmpty()){  // 注册成功，跳转首页
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请近快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{  // 注册失败，返回注册界面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));

            return "/site/register";
        }
    }



    // 激活路径 -> http://localhost:8080/community/activation/101/code(激活码)
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if(result == ACTIVATION_SUCCESS){   // 成功跳到登录页面
            model.addAttribute("msg","激活成功，您的账号可以正常使用了！");
            model.addAttribute("target","/login");
        } else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已经被激活过了！");
            model.addAttribute("target","/index");
        } else {    // 失败跳到首页
            model.addAttribute("msg","激活失败，您提供的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }


    // 生成验证码方法
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*HttpSession session*/){
        // 生成验证码以及相应图片
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        /*// 将验证码存入session
        session.setAttribute("kaptcha",text);*/

        // 将验证码存入redis 重构方法
        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);       // 生存时间
        cookie.setPath(contextPath);// 有效路径
        response.addCookie(cookie);
        // 将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60,TimeUnit.SECONDS);

        // 将图片直接输出给浏览器
        response.setContentType("image/png");   // 输出类型
        try {
            OutputStream os = response.getOutputStream();   // 获取输出流
            ImageIO.write(image, "png", os);     // 输出图片
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }

    }


    @RequestMapping(path = "/login", method = RequestMethod.POST) //可以相同访问路径，但是必须不同的方法
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
        // 判断验证码
        /*String kaptcha = (String) session.getAttribute("kaptcha");  // 从session中取出验证码*/

        String kaptcha = null;
        // 从redis中取kaptcha(重构)
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";   // 回到登录页面
        }

        // 检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);

        if (map.containsKey("ticket")){ //含有ticket就成功了，否则失败
            // 成功重定向到首页
            // 设置cookie并发送给客户端
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            // 失败回到登录页面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }


    // 忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    // 向邮箱发送验证码
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session){     // 服务器端需要保存验证码和用户填写的验证码进行校验
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1,"邮箱不能为空！");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email",email);
        // 验证码
        String code = CommunityUtil.generateUUID().substring(0,4);
        context.setVariable("code",code);
        String content = templateEngine.process("/mail/forget",context);    // 使用thymeleaf模板引擎生成的html文件作为邮件内容
        mailClient.sendMail(email,"找回密码",content);

        // 服务端保存验证码
        session.setAttribute("verifyCode",code);

        return CommunityUtil.getJSONString(0);
    }

    // 重置密码
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password,
                                Model model, HttpSession session){
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(verifyCode) || !code.equalsIgnoreCase(verifyCode)){
            model.addAttribute("codeMsg","验证码错误！");
            return "/site/forget";  // 返回忘记密码界面
        }
        Map<String,Object> map = userService.resetPassword(email,password);
        if (map.containsKey("user")){
            // 修改成功，重定向到登录页面
            return "redirect:/login";
        }else{
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";  // 返回忘记密码页面
        }
    }
}
