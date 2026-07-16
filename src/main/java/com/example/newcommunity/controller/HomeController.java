package com.example.newcommunity.controller;

import com.example.newcommunity.dao.DiscussPostMapper;
import com.example.newcommunity.entity.DiscussPost;
import com.example.newcommunity.entity.Page;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.service.DiscussPostService;
import com.example.newcommunity.service.LikeService;
import com.example.newcommunity.service.UserService;
import com.example.newcommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        //SpringMVC自动将Page注入Model
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");


        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        System.out.println(list);
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if(list!=null){
            for(DiscussPost post:list){
                Map<String,Object> map=new HashMap<>();
                System.out.println(post);
                map.put("post",post);
                User user= userService.findUserById(post.getUserId());
                System.out.println(post.getUserId());
                System.out.println(user);
                map.put("user",user);
                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
                System.out.println(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }
}
