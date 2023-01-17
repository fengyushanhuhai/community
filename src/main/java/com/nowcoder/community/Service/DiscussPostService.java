package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 查询帖子
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    // 查询某用户发帖数量
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostCount(userId);
    }
}
