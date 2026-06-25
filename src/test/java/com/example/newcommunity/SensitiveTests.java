package com.example.newcommunity;

import com.example.newcommunity.util.SensetiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes=NewCommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensetiveFilter sensetiveFilter;

    @Test
    public void testFilter(){
        String text="你好,我是一个敏感词,可以嫖娼可以赌博abd";
        text=sensetiveFilter.filter(text);
        System.out.println(text);
    }



}
















