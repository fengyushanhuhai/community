package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    //根据评论的实体来查询评论的集合
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    // 当前帖子的评论数据的条目数
    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    // 增加评论(需要进行两次DML操作，需要进行事务管理，保证两次操作在一次事务内完成要么全成功，要么都失败)
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        // 过滤html标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子的评论数量 只有帖子才有评论数显示
        // 如果类型是帖子
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            // 查到帖子数量
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            // 更新帖子
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    // 通过id获取评论
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    // 获取user的全部发言
    public List<Comment> findUserComments(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUser(userId, offset, limit);
    }

    // 获取user发言的全部数量
    public int findUserCount(int userId) {
        return commentMapper.selectCountByUser(userId);
    }
}
