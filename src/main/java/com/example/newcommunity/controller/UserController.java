package com.example.newcommunity.controller;

import com.example.newcommunity.annotation.LoginRequired;
import com.example.newcommunity.entity.User;
import com.example.newcommunity.service.FollowService;
import com.example.newcommunity.service.LikeService;
import com.example.newcommunity.service.UserService;
import com.example.newcommunity.util.CommunityUtil;
import com.example.newcommunity.util.HostHolder;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.example.newcommunity.util.CommunityConstant.ENTITY_TYPE_USER;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger=LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) throws IOException {
        if(headerImage.isEmpty()){
            model.addAttribute("error","图片不能为空");
            return "/site/setting";
        }
        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //获取图片名称后缀
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","图片格式错误");
            return "/site/setting";
        }
        //生成随机文件名防止重名
        fileName= CommunityUtil.generateUUID()+suffix;
        //确定文件存放路径
        File dest=new File(uploadPath+"/"+fileName);
        try{
            headerImage.transferTo(dest);
        }catch (IOException e){
            logger.error("上传图片失败"+e.getMessage());
            model.addAttribute("error","上传图片失败");
            throw new RuntimeException("上传图片失败",e);
        }
        //更新当前用户头像的web路径
        User user=hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        //文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try(
                OutputStream os=response.getOutputStream();
                FileInputStream fis=new FileInputStream(fileName);
                )
        {
            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }

    //个人主页
    @RequestMapping(path="/profile/{userId}",method= RequestMethod.GET)
    public String getProfilePage(@PathVariable int userId, Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }

        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount=likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount=followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数量
        long followerCount=followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        //是否已关注
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "site/profile";
    }



}
