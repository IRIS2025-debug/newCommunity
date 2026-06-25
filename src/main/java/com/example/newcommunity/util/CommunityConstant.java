package com.example.newcommunity.util;

public interface CommunityConstant {

    //激活成功
    int ACTIVATION_SUCCESS = 0;
    //重复激活
    int ACTIVATION_REPEAT = 1;
    //激活失败
    int ACTIVATION_FAILED = 2;

    int DEFAULT_EXPIRED_SECONDS = 60 * 10;
    //默认记住登录状态的过期时间
    int REMEMBER_EXPIRED_SECONDS = 60 * 60 * 24 * 100;
}
