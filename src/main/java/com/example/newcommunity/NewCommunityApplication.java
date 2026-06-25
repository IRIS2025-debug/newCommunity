package com.example.newcommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.newcommunity.dao")
public class NewCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCommunityApplication.class, args);
    }

}
