package com.example.newcommunity.service;

import com.example.newcommunity.dao.DiscussPostMapper;
import com.example.newcommunity.entity.DiscussPost;
import com.example.newcommunity.util.SensetiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private SensetiveFilter sensetiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }


    @Transactional
    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        logger.info("准备发布帖子: userId={}, title={}, contentLength={}", post.getUserId(), post.getTitle(), post.getContent()!=null?post.getContent().length():"null");
        //转义html标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        logger.info("HTML转义后: title={}, contentLength={}", post.getTitle(), post.getContent()!=null?post.getContent().length():"null");
        //过滤敏感词
        post.setTitle(sensetiveFilter.filter(post.getTitle()));
        post.setContent(sensetiveFilter.filter(post.getContent()));
        logger.info("敏感词过滤后: title={}, contentLength={}", post.getTitle(), post.getContent()!=null?post.getContent().length():"null");
        //插入数据
        int rows = discussPostMapper.insertDiscussPost(post);
        logger.info("插入结果: rows={}, postId={}", rows, post.getId());
        return rows;
    }

}