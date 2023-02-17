package com.nowcoder.community;

import com.nowcoder.community.Dao.DiscussPostMapper;
import com.nowcoder.community.Dao.LoginTicketMapper;
import com.nowcoder.community.Dao.MessageMapper;
import com.nowcoder.community.Dao.UserMapper;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
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

    @Test
    public void testInsertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(164);
        discussPost.setTitle("笑死");
        discussPost.setContent("元宵节还上班！");
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        int x = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(x);
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessageOrLetter(){
       List<Message> list =  messageMapper.selectConversations(111,0,20);
       for (Message message : list){
           System.out.println(message);
       }

       int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112",0,10);
        for (Message message : list){
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }


}
