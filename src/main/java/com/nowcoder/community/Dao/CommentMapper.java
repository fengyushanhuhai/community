package com.nowcoder.community.Dao;
import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 需要分页查询 查询一共有多少条数据 以及 每一页有多少条数据

    // 根据评论的实体来查询评论的集合
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    // 当前帖子的评论数据的条目数
    int selectCountByEntity(int entityType, int entityId);

    // 增加评论的方法
    int insertComment(Comment comment);

    // 通过帖子id获取评论
    Comment selectCommentById(int id);

    // 获取某个用户的曾经发布过的帖子
    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    // 获取帖子的发布数
    int selectCountByUser(int userId);

}
