package com.example.newcommunity.service;

import com.example.newcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId){
      //先把key拼出来
      String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
      //是不是点过赞了
      boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
      if(isMember){
          redisTemplate.opsForSet().remove(entityLikeKey,userId);
      } else {
          redisTemplate.opsForSet().add(entityLikeKey,userId);
      }
    }

    //查询实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }




}
