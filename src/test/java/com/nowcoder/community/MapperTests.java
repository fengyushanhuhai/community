package com.nowcoder.community;

import com.nowcoder.community.Dao.DiscussPostMapper;
import com.nowcoder.community.Dao.LoginTicketMapper;
import com.nowcoder.community.Dao.UserMapper;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	// 在测试类中可以启动配置类
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int row = userMapper.UpdateStatus(150,1);
        System.out.println(row);
        row = userMapper.UpdateHeader(150,"http://www.nowcoder.com/102.png");
        System.out.println(row);
        row = userMapper.UpdatePassword(150,"hello");
        System.out.println(row);

    }

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10);  // 查询第0行开始的十条数据
        for(DiscussPost discussPost : list){
            System.out.println(discussPost);
        }

        System.out.println(discussPostMapper.selectDiscussPostCount(149));// userId 为0说明不在当前筛选条件
    }


    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("asfas");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 *10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("asfas");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("asfas",1);
        loginTicket = loginTicketMapper.selectByTicket("asfas");
        System.out.println(loginTicket);

    }


}
