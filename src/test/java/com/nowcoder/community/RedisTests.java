package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	// 在测试类中可以启动配置类
public class RedisTests {


    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testString(){
        String redisKey = "test:count";

        // 向redis存入数据
        redisTemplate.opsForValue().set(redisKey,1);

        // 取出数据
        System.out.println(redisTemplate.opsForValue().get(redisKey));

        // 增加
        System.out.println(redisTemplate.opsForValue().increment(redisKey));

        // 减少
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
        public void testHash(){
        String redisKey = "test:user";

        // 向redis存入数据
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");
        redisTemplate.opsForHash().put(redisKey,"age",11);

        // 取出数据
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"age"));
    }

    // 访问列表
    @Test
    public void testList(){
        String redisKey = "test:ids";

        // 向redis存入数据
        redisTemplate.opsForList().leftPush(redisKey,101);      // 从左边存入数据
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        // 取出数据
        System.out.println(redisTemplate.opsForList().size(redisKey));  // 获取一共有多少条数据
        System.out.println(redisTemplate.opsForList().index(redisKey,0));  // 获取某索引所对应的数据
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));  // 获取某范围的数据
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));  // 弹出数据（从左边弹出数据）
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));  //
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));  //
        System.out.println(redisTemplate.opsForList().size(redisKey));  // 获取一共有多少条数据
    }

    // 访问集合set
    @Test
    public void testSet(){
        String redisKey = "test:teachers";

        // 向redis存入数据
        redisTemplate.opsForSet().add(redisKey,"刘备","张飞","关羽","诸葛亮");      // 从左边存入数据


        // 取出数据
        System.out.println(redisTemplate.opsForSet().size(redisKey));  // 获取一共有多少条数据

        System.out.println(redisTemplate.opsForSet().pop(redisKey));    // 随机弹出数据

        System.out.println(redisTemplate.opsForSet().members(redisKey));   // 统计集合中所有的数据是什么
    }

    // 访问有序集合
    @Test
    public void testSortedSet(){
        String redisKey = "test:students";

        // 向redis存入数据
        redisTemplate.opsForZSet().add(redisKey,"汤森",80);      // 从左边存入数据
        redisTemplate.opsForZSet().add(redisKey,"悟空",90);      // 从左边存入数据
        redisTemplate.opsForZSet().add(redisKey,"八戒",50);      // 从左边存入数据
        redisTemplate.opsForZSet().add(redisKey,"沙森",70);      // 从左边存入数据
        redisTemplate.opsForZSet().add(redisKey,"小白",60);      // 从左边存入数据


        // 取出数据
        System.out.println(redisTemplate.opsForZSet().size(redisKey));  // 获取一共有多少条数据
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"八戒"));  // 统计某一个的分数
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"八戒"));  // 由大到小排名  z注意返回的是索引，不是真正的排名
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,1));  // 由大到小倒序排名


    }

    // 访问有序集合
    @Test
    public void testKeys(){
        // 删除key
        redisTemplate.delete("test:user");

        // 判断是否含有这个key
        System.out.println(redisTemplate.hasKey("test:user"));

        // 设置过期时间 10s过期
        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);



    }


    // 多次访问同一个key 绑定到一个对象
    @Test
    public void testBoundKey(){
        String key = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(key);     // 绑定key
        // 绑定之后的操作就不用出入参数key了
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // 编程型事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                // 启用事务
                operations.multi();

                // 进行处理
                // 开启事务和提交事务之间是将命令放在队列中此时查询操作没有效果
                // 在管理事务中不要左查询操作
                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"wangwu");
                operations.opsForSet().add(redisKey,"laoliu");

                System.out.println(operations.opsForSet().members(redisKey));

                // 提交事务
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
