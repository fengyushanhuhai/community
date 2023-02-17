package com.nowcoder.community.Service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    // 添加关注
    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 构造目标的key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                // 粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                operations.exec();
                return null;
            }
        });
    }


    // 取消关注
    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 构造目标的key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                // 粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();
                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);
                operations.exec();
                return null;
            }
        });
    }


    // 查询某个用户关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询某个实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户有没有关注目标这个状态
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;      // 不为空就是关注了，为空就是没有关注
    }


    // 查询某用户关注的人
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        // 倒序 获取关注目标的id
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey,offset, offset + limit - 1);
        if (targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);

            // 关注的时间
            Double score = redisTemplate.opsForZSet().score(followeeKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


    // 查询粉丝数
    public List<Map<String,Object>> findFollowers(int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        // 倒序 获取关注目标的id
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset, offset + limit - 1);
        if (targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);

            // 关注的时间
            Double score = redisTemplate.opsForZSet().score(followerKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }



















}
