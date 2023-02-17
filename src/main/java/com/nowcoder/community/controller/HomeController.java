package com.nowcoder.community.controller;

import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    // 将业务组件注入
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // 方法调用之前，springmvc自动实例化Model和Page，并且自动将Page注入给Model，
        // 所以在thymeleaf模板中就可以自动访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0)); // 获取一共有多少条数据
        page.setPath("/index");     // 设置查询路劲，用来复用分页的链接

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost discussPost : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post",discussPost);    // 帖子
                User user = userService.findUserById(discussPost.getUserId());  // 根据发帖子的userId获取发帖子的用户信息
                map.put("user",user);

                // 点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";    // /templates 不用写
    }

    // 错误的页面 返回500
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";

    }


}
