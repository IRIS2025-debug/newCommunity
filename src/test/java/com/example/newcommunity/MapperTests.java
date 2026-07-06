package com.example.newcommunity;

import com.example.newcommunity.dao.LoginTicketMapper;
import com.example.newcommunity.dao.MessageMapper;
import com.example.newcommunity.entity.LoginTicket;
import com.example.newcommunity.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = NewCommunityApplication.class)
public class MapperTests {
    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test

    public void testLoginTicketMapper(){


        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("123456");
        loginTicket.setStatus(0);
        loginTicket.setUserId(101);
        loginTicket.setExpired(new Timestamp(System.currentTimeMillis() + 1000 * 60 * 10));
        int rows = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(rows);
        LoginTicket ticket = loginTicketMapper.selectByTicket("123456");
        System.out.println(ticket);
        int rows2 = loginTicketMapper.updateStatus("123456", 1);
        System.out.println(rows2);
        ticket = loginTicketMapper.selectByTicket("123456");
        System.out.println(ticket);
        System.out.println(ticket.getStatus());
        System.out.println(ticket.getUserId());
        System.out.println(ticket.getExpired());
        System.out.println(ticket.getId());
        System.out.println(ticket.getTicket());
    }

    @Test
    public void testSelectLetters(){
        //111的所有会话
        List<Message> list=messageMapper.selectConversations(111,0,10);
        for(Message message:list){
            System.out.println(message);
        }

        //111的所有会话数量
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        //111和112的私信
        list = messageMapper.selectLetters("111_112",0,10);
        for(Message message:list){
            System.out.println(message);
        }

        //111和112的私信数量
        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        //113发给111的未读私信数量
        count=messageMapper.selectLetterUnreadCount(111,"111_131");
        System.out.println(count);
    }


}
