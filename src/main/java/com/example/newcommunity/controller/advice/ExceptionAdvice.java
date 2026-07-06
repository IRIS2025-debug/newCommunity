package com.example.newcommunity.controller.advice;

import com.example.newcommunity.util.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations= Controller.class)
public class ExceptionAdvice {

    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常:"+e.getMessage());
        for(StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }
        String xRequestedWith=request.getHeader("X-Requested-With");
        //异步Ajax请求，因为只更新局部，所以不能直接重定向到错误页面
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else{
            //这种情况为普通请求，返回的是html，重定向到错误页面
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}



















