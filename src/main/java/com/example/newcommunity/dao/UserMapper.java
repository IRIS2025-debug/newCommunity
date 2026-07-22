package com.example.newcommunity.dao;

import com.example.newcommunity.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);

    void updateStatus(int id,int status);
    int updateHeader(int id, String headerUrl);
}
