package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(
                req.getUsername(), req.getPassword(),
                req.getEmail(), req.getPhone(), req.getAddress());
        user.setPassword(null);      // 不返回密码
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     * POST /api/users/login
     * 返回 Token 和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        User user = userService.findByUsername(req.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return Result.success("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/users/me
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = userService.getById(userId);
        user.setPassword(null);      // 不返回密码
        return Result.success(user);
    }

    /**
     * 更新个人信息
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public Result<User> updateProfile(@RequestBody User updateUser,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        updateUser.setId(userId);
        // 不允许修改角色和密码
        updateUser.setRole(null);
        updateUser.setPassword(null);
        userService.updateById(updateUser);
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    /**
     * 管理员：获取所有用户列表
     * GET /api/users
     */
    @GetMapping
    public Result<List<User>> listAll(HttpServletRequest request) {
        checkAdmin(request);
        List<User> users = userService.list();
        users.forEach(u -> u.setPassword(null));  // 清除密码
        return Result.success(users);
    }

    /**
     * 管理员：删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        userService.removeById(id);
        return Result.success("删除成功", null);
    }

    /** 检查是否为管理员，不是则抛出异常 */
    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限，仅管理员可操作");
        }
    }
}
