package com.example.newcommunity.controller;

import com.example.newcommunity.entity.Event;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.event.EventProducer;
import com.example.newcommunity.service.LikeService;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static com.example.newcommunity.util.CommunityConstant.TOPIC_LIKE;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user=hostHolder.getUser();

        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

        //数量
        long likeCount=likeService.findEntityLikeCount(entityType,entityId);

        //状态
        int likeStatus=likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        //把数量和状态统一传给前端
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        if(likeStatus==1){
            Event event = new Event().setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityId(entityId)
                    .setEntityType(entityType)
                    .setData("entityUserId",entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);

        }

        return CommunityUtil.getJSONString(0,null,map);

    }
}
