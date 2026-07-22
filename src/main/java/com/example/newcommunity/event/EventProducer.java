package com.example.newcommunity.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.newcommunity.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    //处理事件
    public void fireEvent(Event event) {
        // 发送事件到Kafka
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }


}
