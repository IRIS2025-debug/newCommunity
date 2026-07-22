package com.example.newcommunity.service;

import com.example.newcommunity.dao.CommentMapper;
import com.example.newcommunity.entity.Comment;
import com.example.newcommunity.util.CommunityConstant;
import com.example.newcommunity.util.SensetiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensetiveFilter sensetiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义HTML标记
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤敏感词
        comment.setContent(sensetiveFilter.filter(comment.getContent()));
        //插入数据
        int rows = commentMapper.insertComment(comment);
        //更新帖子评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //更新帖子评论数量
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

}
