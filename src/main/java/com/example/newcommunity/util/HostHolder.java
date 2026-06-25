package com.example.newcommunity.util;

import com.example.newcommunity.entity.User;
import org.springframework.stereotype.Component;
/**
 * 持有用户信息的工具类,用于代替Session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
