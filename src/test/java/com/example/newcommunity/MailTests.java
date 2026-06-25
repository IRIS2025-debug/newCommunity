package com.example.newcommunity;

import com.example.newcommunity.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootTest
@ContextConfiguration(classes = NewCommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Test
    public void testSendMail() {
        mailClient.sendMail("3388372404@qq.com", "you are so great and i love you", "This is a test email from NewCommunity.");

    }

    @Test
    public void testSendHtmlMail() {
        Context context=new Context();
        context.setVariable("username","sunday");
        String content =templateEngine.process("mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("3388372404@qq.com", "you are so great and i love you", content);
    }
}