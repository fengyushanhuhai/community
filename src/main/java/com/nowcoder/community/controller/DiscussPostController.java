package com.nowcoder.community.controller;

import com.nowcoder.community.Service.CommentService;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    // 默认新增的帖子只有标题和内容
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"尚未登录！");
        }

        // 保存帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        // 报错的情况将来同一处理
        return CommunityUtil.getJSONString(0,"消息发布成功！");
    }


    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        // 将帖子传给模板
        model.addAttribute("post",post);

        // 查帖子的作者
        User user = userService.findUserById(post.getUserId());
        // 将作者信息传给模板
        model.addAttribute("user",user);

        // 查点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);

        // 查点赞状态
        // 注意用户要登录
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        // 查评论的分页信息
        page.setLimit(5);                                   // 每页显示5条
        page.setPath("/discuss/detail/" + discussPostId);   // 路径
        page.setRows(post.getCommentCount());               // 帖子的总评论数从而算出页数


        // 给帖子的评论==> 评论
        // 给评论的评论==> 回复
        // 评论列表
        List<Comment> commentList  = commentService.findCommentByEntity(
                ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        // 评论的VO列表 viewObject 可视化对象
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        if (commentVOList != null){
            for (Comment comment : commentList){
                // 一条评论的VO
                Map<String,Object> commentVO = new HashMap<>();
                // 评论
                commentVO.put("comment",comment);
                // 作者
                commentVO.put("user",userService.findUserById(comment.getUserId()));

                // 查点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("likeCount",likeCount);

                // 查点赞状态
                // 注意用户要登录
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("likeStatus",likeStatus);

                // 回复列表 不分页
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(),0,Integer.MAX_VALUE);
                // 回复的VO列表
                List<Map<String,Object>> replyVOList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply : replyList){
                        Map<String,Object> replyVO = new HashMap<>();
                        // 回复
                        replyVO.put("reply",reply);
                        // 回复的作者
                        replyVO.put("user",userService.findUserById(reply.getUserId()));
                        // 回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target",target);
                        // 查点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVO.put("likeCount",likeCount);

                        // 查点赞状态
                        // 注意用户要登录
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVO.put("likeStatus",likeStatus);

                        // 装入list中
                        replyVOList.add(replyVO);
                    }
                }
                commentVO.put("replys",replyVOList);

                // 回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("replyCount",replyCount);


                commentVOList.add(commentVO);
            }
        }
        model.addAttribute("comments",commentVOList);

        // 返回模板路径
        return "/site/discuss-detail";
    }
}
