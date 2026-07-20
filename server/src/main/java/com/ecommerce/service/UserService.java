package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.User;

/**
 * 用户服务接口
 * IService 提供了 save/update/remove/getById/list 等通用方法
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    User register(String username, String password, String email, String phone, String address);

    /** 用户登录，成功返回 Token，失败返回 null */
    String login(String username, String password);

    /** 根据用户名查询用户 */
    User findByUsername(String username);
}
