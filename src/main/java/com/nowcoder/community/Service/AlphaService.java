package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.AlphaDao;
import com.nowcoder.community.Dao.DiscussPostMapper;
import com.nowcoder.community.Dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

@Service
public class AlphaService {



    @Autowired
    @Qualifier("alphaHibernate")
    private AlphaDao alphaDao;

    public String useDao(){
        return alphaDao.select();
    }


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 两种管理事务的方法 任何地方报错都要回滚

    // 声明隔离级别和传播机制
    // 传播机制 propagation
    // REQUIRED         支持外部事务(a调b，a的事务相对于b就是当前事务也就是外部事务)，外部事务不存在就创建新事务
    // REQUIRES_NEW     创建一个新的事务并且暂停外部事务(a调b，b无视a的事务)
    // NESTED           如果当前存在外部事务，则嵌套在该事务中执行（a调b，若a有事务，则b嵌套在a中执行，并且具有独立的提交和回滚）; 如果不存在，就和required一样了
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)    // 第二个基本的隔离性
    public Object save1(){
        // 逻辑 新增用户再加帖子
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
        user.setEmail("21354@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/11t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        // 新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hell");
        discussPost.setContent("evil come in");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);


        // 当程序发生异常保证了完整的回滚 user和post都没有成功插入数据库
        Integer.valueOf("abc");

        return "ok";
    }

    @Autowired
    private TransactionTemplate transactionTemplate;

    public Object save2(){
        // 设置隔离级别和传播机制
        transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);


        // 回调方法
        return  transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 逻辑 新增用户再加帖子
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
                user.setEmail("21354@qq.com");
                user.setHeaderUrl("http://images.nowcoder.com/head/11t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                // 新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hell");
                discussPost.setContent("evil come in");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);


                // 当程序发生异常保证了完整的回滚 user和post都没有成功插入数据库
                Integer.valueOf("abc");
                return "ok";
            }
        });
    }
}
