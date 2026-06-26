package com.example.newcommunity.controller;

import com.example.newcommunity.entity.DiscussPost;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.service.DiscussPostService;
import com.example.newcommunity.service.UserService;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //处理增加帖子的请求
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user=hostHolder.getUser();
        if(user==null){
            logger.warn("用户尝试发布帖子但未登录");
            return CommunityUtil.getJSONString(403,"你还没有登录，请先登录");
        }

        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0); // 默认类型
        post.setStatus(0); // 默认状态
        post.setCreateTime(new Date());
        post.setCommentCount(0);
        post.setScore(0);
        logger.info("准备发布帖子: userId={}, username={}, title={}", user.getId(), user.getUsername(), post.getTitle());
        try {
            int rows = discussPostService.addDiscussPost(post);
            logger.info("帖子发布成功: rows={}, postId={}", rows, post.getId());
            return CommunityUtil.getJSONString(200, "发布成功");
        } catch (Exception e) {
            logger.error("帖子发布失败", e);
            return CommunityUtil.getJSONString(500, "发布失败: " + e.getMessage());
        }
    }

    //因为直接把参数拼接到路径中了，所以用get方式访问url
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable int discussPostId, Model model)
    {
        //帖子
        DiscussPost discussPost=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        //作者
        User user=userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        return "/site/discuss-detail";

    }

}