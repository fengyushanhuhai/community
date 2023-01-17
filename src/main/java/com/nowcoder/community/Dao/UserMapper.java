package com.nowcoder.community.Dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper // @Repository这个是spring提供的注解，但是mybatis自己有提供注解让spring容器扫描到
public interface UserMapper {


    // 根据id查询用户
    User selectById(int id);

    // 根据用户名查用户
    User selectByName(String username);

    // 根据邮箱查用户
    User selectByEmail(String email);

    // 增加一个用户,返回插入数据的行数
    int insertUser(User user);

    // 修改user状态,返回修改行数
    int UpdateStatus(int id, int status);

    // 更新头像的url
    int UpdateHeader(int id, String headerUrl);

    // 修改密码
    int UpdatePassword(int id, String password);
}
