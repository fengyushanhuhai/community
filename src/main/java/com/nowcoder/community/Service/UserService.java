package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


    // 根据用户id查询用户
    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
