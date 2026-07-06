package com.example.newcommunity.dao;

import com.example.newcommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表,针对每个会话只返回一条最新的消息（整页展示）
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //某个会话包含的所有私信
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //某个会话包含的所有私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //新增一个消息
    int insertMessage(Message message);

    //把未读的消息设置为已读的/修改消息的状态
    int updateStatus(List<Integer> ids,int status);
}
