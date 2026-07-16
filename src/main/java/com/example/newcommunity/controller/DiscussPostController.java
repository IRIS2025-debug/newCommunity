package com.example.newcommunity.controller;

import com.example.newcommunity.entity.Comment;
import com.example.newcommunity.entity.DiscussPost;
import com.example.newcommunity.entity.Page;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.service.CommentService;
import com.example.newcommunity.service.DiscussPostService;
import com.example.newcommunity.service.LikeService;
import com.example.newcommunity.service.UserService;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.newcommunity.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.example.newcommunity.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

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

    //因为直接把参数拼接到路径中了，所以用get方式访问url,查询帖子详情
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable int discussPostId, Model model, Page page)
    {
        //帖子
        DiscussPost discussPost=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        //作者
        User user=userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //点赞数量
        long likeCount= likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);

        //点赞状态
        int likeStatus=likeService.findEntityLikeStatus(hostHolder.getUser()==null?0:hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //开发顺序：entity-dao(mapper)-xml-service-controller-view
        //查评论的信息,完善分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());

        //找到该帖子的当页所有评论,是一个Comment对象的List
        List<Comment> commentList=commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit());

        /*
         * 总逻辑：
         * 先查评论，返回一个Comment对象的List
         * 接着把List<Comment>封装成List<Map<String,Object>>
         * 在这个过程中
         * 要封装帖子评论的作者，内容，评论的评论（同样的原理封装作者，内容，以及target）
         * 封装之后add到commentVoList
        **/
        //将Comment对象转换为Map对象(类似于JSON数组),方便在前端渲染
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo=new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount= likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);

                //点赞状态
                likeStatus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser()==null?0:hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);


                //评论的评论
                List<Comment> replyList=commentService.findCommentsByEntity(
                        2, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                //将评论的评论转换为Map对象(类似于JSON数组),方便在前端渲染
                if(replyList!=null){
                    for(Comment reply:replyList){
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞数量
                        likeCount= likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus=hostHolder.getUser().getId()==0?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                //将评论的评论添加到评论的map中
                commentVo.put("replys", replyVoList);
                //回复数量
                int replyCount=commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                //将评论添加到评论的列表中
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";

    }

}