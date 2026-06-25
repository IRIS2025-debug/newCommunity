package com.example.newcommunity;

import com.example.newcommunity.dao.LoginTicketMapper;
import com.example.newcommunity.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;

@SpringBootTest
@ContextConfiguration(classes = NewCommunityApplication.class)
public class MapperTests {
    @Autowired
    LoginTicketMapper loginTicketMapper;

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
}
