package com.example.newcommunity.controller;

import com.example.newcommunity.entity.User;
import com.example.newcommunity.service.UserService;
import com.example.newcommunity.util.CommunityConstant;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private Producer kaptcha;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("user", user);
            return "/site/register";
        }

    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(Model model, @PathVariable int userId, @PathVariable String activationCode) {
        int result=userService.activation(userId,activationCode);
        if(result== CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,您的帐号已经可以正常使用了");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else if(result==CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg","无效的操作，该账号已经激活过了");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else{
            model.addAttribute("msg","激活失败,您提供的激活码不正确！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        String text=kaptcha.createText();
        BufferedImage image=kaptcha.createImage(text);

        //session.setAttribute("kaptchaText",text);
        //验证码的归属
        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码写入redis
        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        try{
            ImageIO.write(image,"png",response.getOutputStream());
        }catch (IOException e){
            logger.error(()->"获取验证码失败"+e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberMe,Model model,
                        HttpSession session,HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){
        //从redis中获取验证码
        String kaptcha=null;
        if(kaptchaOwner!=null){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha=(String) redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equals(code)){
            model.addAttribute("codeMsg","验证码错误");
            model.addAttribute("target","/login");
            return "/site/login";
        }

        int expiredSeconds=rememberMe?CommunityConstant.REMEMBER_EXPIRED_SECONDS:CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket")){
            //登录成功
            Cookie cookie=new Cookie("ticket",(String) map.get("ticket"));
            cookie.setPath(contextPath);//访问任何页面该cookie都有效
            cookie.setMaxAge(expiredSeconds);//过期时间
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";//默认get请求
    }


}

