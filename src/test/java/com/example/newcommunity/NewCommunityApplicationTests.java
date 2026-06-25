package com.example.newcommunity;

import com.example.newcommunity.dao.DiscussPostMapper;
import com.example.newcommunity.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = NewCommunityApplication.class)
public class NewCommunityApplicationTests {

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Test
    void testSelectDiscussPost() {
        // 方法名必须和你的 Mapper 接口一致：selectDiscussPosts
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);

        for (DiscussPost post : discussPosts) {
            System.out.println(post);
        }

        System.out.println(discussPostMapper.selectDiscussPostRows(0));
    }
}
