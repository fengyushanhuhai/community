package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.LoginTicketMapper;
import com.nowcoder.community.Dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    // 根据用户id查询用户
    public User findUserById(int id){
        // 将用户应该缓存到redis 提高效率
        // return userMapper.selectById(id);

        // 先从cache中查
        User user = getCache(id);
        if (user == null){
            user = initCache(id);   // 如果缓存中没有查到说明要初始化缓存数据
        }
        return user;
    }

    // 根据用户名查询用户
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }


    // 1、优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }


    // 2、取不到就从初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);      // 并且给一个过期时间
        return user;
    }
    // 3、数据变化时清除缓存数据
    private void CacheClear(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


    @Autowired
    private MailClient mailClient;  // 邮件客户端

    @Autowired
    private TemplateEngine templateEngine;  // 模板引擎

    // 将域名、项目名注入进来用于生成激活码
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 注册
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        // 对参数进行判断
        // 空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());   // 通过当前账号查数据库中是否存在该账号
        if(u != null){  // 数据库中有该账号
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){  // 数据库中有该账号
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5)); // 生成5位salt
        //加密
        user.setPassword(CommunityUtil.MD5(user.getPassword() + user.getSalt())); // 原始密码再加上salt
        // 补充user的其他数据并传入数据库
        user.setType(0);
        user.setStatus(0);  // 表示未激活
        user.setActivationCode(CommunityUtil.generateUUID());   // 激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));// 设置随机头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件给用户
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // 激活路径 -> http://localhost:8080/community/activation/101/code(激活码)
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();  // 本来是没有id的，但是传入数据库后mybatis可以自动回填 mybatis.configuration.useGeneratedKeys=true
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }


    // 激活
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){      // 如果已经激活，返回重复
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(code)){   // 成功激活
            userMapper.UpdateStatus(userId,1);
            CacheClear(userId); // 清理redis缓存
            return ACTIVATION_SUCCESS;
        } else {                            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }


    // 凭证
    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String,Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        // 验证账号合法性
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }

        // 验证账号是否激活
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        // 验证密码
        password = CommunityUtil.MD5(password + user.getSalt());    // 得到加密后密码
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        // 登录成功，生成登录凭证服务器和客户端都要有
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());                    // id
        loginTicket.setTicket(CommunityUtil.generateUUID());    // 凭证为随机字符串
        loginTicket.setStatus(0);                               // 状态
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));   // 过期时间
//        loginTicketMapper.insertLoginTicket(loginTicket);       // 将登录凭证传入数据库
        // 传入redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket); // redis可以将对象序列化一个json字符串

        map.put("ticket",loginTicket.getTicket());      // 将ticket给客户端
        return map;
    }

    // 退出
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);       // 1 表示无效
        // redis取出来修改后再传回去
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);  // 默认返回的类型是Object
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket); // redis可以将对象序列化一个json字符串
    }

    // 重置密码
    public Map<String,Object> resetPassword(String email,String password){
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg","该邮箱尚未注册！");
            return map;
        }
        // 重置密码
        password = CommunityUtil.MD5(password + user.getSalt());
        userMapper.UpdatePassword(user.getId(),password);
        // 清理redis缓存
        CacheClear(user.getId());

        map.put("user", user);
        return map;
    }


    // 查询凭证
    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }


    // 更新用户的头像路径 返回修改的行数
    public int updateHeader(int userId, String headerUrl){
        int rows =  userMapper.UpdateHeader(userId,headerUrl);
        // 清理redis缓存
        CacheClear(userId);
        return rows;
    }


    // 修改密码
    public Map<String,Object> updatePassword(int userId, String oldPassword, String newPassword){
        Map<String,Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码不能为空！");
            return map;
        }

        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空！");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.MD5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg","原密码输入错误！");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.MD5(newPassword + user.getSalt());
        userMapper.UpdatePassword(userId,newPassword);
        // 清理redis缓存
        CacheClear(userId);
        return map;
    }



}
