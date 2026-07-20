package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.UserService;
import com.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * ServiceImpl<Mapper, Entity> 提供了通用的 CRUD 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     * 1. 检查用户名是否已存在
     * 2. 保存用户信息
     */
    @Override
    public User register(String username, String password, String email, String phone, String address) {
        // 检查用户名是否已被注册
        User existUser = findByUsername(username);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);     // 注意：实际项目中应对密码加密后再存储
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole("CUSTOMER");       // 默认注册为普通用户

        // 保存到数据库
        save(user);
        return user;
    }

    /**
     * 用户登录
     * 1. 根据用户名查找用户
     * 2. 校验密码
     * 3. 生成 JWT Token
     */
    @Override
    public String login(String username, String password) {
        // 查找用户
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成并返回 Token
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}
