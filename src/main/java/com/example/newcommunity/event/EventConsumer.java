package com.example.newcommunity.event;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.newcommunity.entity.Event;
import com.example.newcommunity.entity.Message;
import com.example.newcommunity.service.MessageService;
import com.example.newcommunity.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleComment(ConsumerRecord record) {
        if(record==null||record.value()==null){
            logger.error("收到空消息");
            return;
        }

        Event event= JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message=new Message();
        message.setFromId(SYSTEM_USER_ID);//消息的发布者
        message.setToId(event.getEntityUserId());//消息的接收者
        message.setConversationId(event.getTopic());//消息的会话ID
        message.setCreateTime(new Date());//消息的创建时间

        Map<String,Object> content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityId",event.getEntityId());
        content.put("entityType",event.getEntityType());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));//消息的内容
        messageService.addMessage(message);//添加消息

    }
}
