package com.example.newcommunity.service;

import com.example.newcommunity.dao.LoginTicketMapper;
import com.example.newcommunity.dao.UserMapper;
import com.example.newcommunity.entity.LoginTicket;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.util.CommunityConstant;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.MailClient;
import com.example.newcommunity.util.RedisKeyUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            //不算错误，所以不用抛异常，把漏洞信息返回即可
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号未被注册
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        //验证邮箱未被注册
        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //前置条件均已完成，可以开始注册
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.newcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("url",domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode());
        String content=templateEngine.process("mail/activation",context);
        mailClient.sendMail(user.getEmail(),"牛客网-激活账号",content);
        return map;
    }

    public int activation(int userId,String activationCode){
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }
        if(!user.getActivationCode().equals(activationCode)){
            return CommunityConstant.ACTIVATION_FAILED;
        }
        //清除缓存
        clearCache(userId);
        user.setStatus(1);
        userMapper.updateStatus(user.getId(),1);
        return CommunityConstant.ACTIVATION_SUCCESS;
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证账号
        User user=userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证状态
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //验证密码
        if(!user.getPassword().equals(CommunityUtil.md5(password+user.getSalt()))){
            map.put("passwordMsg","密码错误");
            return map;
        }
        //登录成功
        map.put("ticket",CommunityUtil.generateUUID());
        map.put("expired",new Timestamp(System.currentTimeMillis()+expiredSeconds* 1000L));
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket((String) map.get("ticket"));
        loginTicket.setExpired((Timestamp) map.get("expired"));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey= RedisKeyUtil.getTicketKey((String) map.get("ticket"));
        redisTemplate.opsForValue().set(redisKey,loginTicket);


        return map;
    }

    public void logout(String ticket){

        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket= (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket= (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }

    public int updateHeader(int userId, String headerUrl){
        //清除缓存

        int rows=userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //优先从redis中取值，再从数据库中取值
    private User getCache(int userId){
        String redisKey= RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //取不到时初始化缓存数据
    private User initCache(int userId){
        String redisKey= RedisKeyUtil.getUserKey(userId);
        User user=userMapper.selectById(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);

    }
}









