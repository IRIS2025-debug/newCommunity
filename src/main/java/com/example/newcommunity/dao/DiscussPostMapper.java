package com.example.newcommunity.dao;

import com.example.newcommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int selectDiscussPostRows(@Param("userId") int userId);

    DiscussPost selectDiscussPostById(@Param("id") int id);

    //增加帖子的方法
    int insertDiscussPost(DiscussPost discussPost);

    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);
}
