package com.example.newcommunity.dao;

import com.example.newcommunity.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);

    void updateStatus(int id,int status);
    void updateHeader(int id,String headerUrl);
}
