package com.nowcoder.community.controller;

import com.nowcoder.community.Service.*;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 上传路径
    @Value("${community.path.upload}")
    private String uploadPath;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目的访问路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    // 访问设置页面
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    // 上传方法
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        // 文件为空
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        // 读取文件名的后缀并判断
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));  // 从文件名包括最后一个点的后面所有即为其后缀
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);   // 将文件写入当前路径，存储文件
        } catch (IOException e) {
            logger.error("上传文件失败！ ==>" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发送异常",e);
        }

        // 更新当前用户头像的路径（web访问 路径）
        // http;//localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        // 更新成功去首页
        return "redirect:/index";
    }

    // 获取头像(就是指网页上头像发生了变化)
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 找到服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        // 获取字节流
        try(
            OutputStream os = response.getOutputStream();           // 输出流
            FileInputStream fis = new FileInputStream(fileName);    // 输入流
        ) {
            // 设置缓冲区
            byte[] buffer = new byte[1024];
            // 游标
            int b = 0;
            while((b = fis.read(buffer)) != -1){    // 每次从文件中读入buffer这么大数据，等于-1就是没的读了
                os.write(buffer,0,b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword" , method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(),oldPassword,newPassword);
        if (map == null || map.isEmpty()){
            // 修改成功 返回登录页面
            return "redirect:/logout";
        }else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }


    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }

        // 将用户的基信息发给页面
        model.addAttribute("user", user);
        // 用户获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 当前用户是否已经关注这个用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){  // 需要判断当前有没有登录
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";

    }



    // 我的帖子
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setPath("/user/mypost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        // 帖子列表
        List<DiscussPost> discussList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (discussList != null) {
            for (DiscussPost post : discussList) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", post);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVOList);
        return "/site/my-post";
    }

    // 我的回复
    @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setPath("/user/myreply/" + userId);
        page.setRows(commentService.findUserCount(userId));

        // 回复列表
        List<Comment> commentList = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost", post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);
        return "/site/my-reply";
    }



















}
