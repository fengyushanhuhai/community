package com.nowcoder.community.Dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {


    /**
     * 首页分页查询帖子功能
     * @param userId 用于查询个人主义中我的帖子功能，而在首页中并不会传这个值;
     * @param offset 每一页起始行的行号
     * @param limit 每一页最多可以选择多少条数据(可以固化下来)，通过总共多少条数据 / 每一页有多少条数据limit 可以得到一共有多少页
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 通过userId查询当前用户一共发了多少帖子
    // @Param注解可以给参数取别名，适合用于动态sql中并且恰巧方法中有且只有一个条件，当前参数必须取别名
    int selectDiscussPostCount(@Param("userId") int userId);
}
