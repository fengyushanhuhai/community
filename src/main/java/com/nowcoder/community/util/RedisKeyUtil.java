package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; // 前缀   帖子和评论统称为实体
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";       // 我关注的目标
    private static final String PREFIX_FOLLOWER = "follower";       // 关注我的粉丝


    private static final String PREFIX_KAPTCHA = "kaptcha";         // 验证码
    private static final String PREFIX_TICKET = "ticket";         // 验证码
    private static final String PREFIX_USER = "user";         // 验证码

    // 某个实体的赞
    // like:entity:entityType:entityId ==> set(userId)
    public static String getEntityLike(int entityType, int entityId){

        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    // 某个用户的赞
    // like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId(用户的id):entityType(关注的实体的类型) ==> zset (entityId, now) 以当前时间为分数
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId --> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner){ // 用户的临时凭证(因为当前用户还没有登录进来，不能串userId)
        return PREFIX_KAPTCHA + SPLIT + owner;

    }

    // 登录的凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }


    // 用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }






























}
