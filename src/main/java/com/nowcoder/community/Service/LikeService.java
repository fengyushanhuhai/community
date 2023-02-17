package com.nowcoder.community.Service;


import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 实现点赞的业务方法
    // 需要进行两次事务操作， 需要保证事务性
    public void like(int userId, int entityType, int entityId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLike(entityType,entityId);
                // 获取被赞者的id
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 查询必须放在事务范围之外，提前先查询了
                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                // 开启事务
                operations.multi();

                if (isMember){
                    // 为true说明点过赞，此时要取消这个赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    // 需要点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    // 增加帖子作者的总赞数
                    operations.opsForValue().increment(userLikeKey);
                }

                // 提交事务
                operations.exec();
                return null;
            }
        });
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLike(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }


    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLike(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 :0;
    }


    // 查询某个用户获得赞的数量

    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);

        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}


