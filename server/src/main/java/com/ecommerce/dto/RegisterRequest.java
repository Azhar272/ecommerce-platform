package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String email;             // 邮箱（可选）

    private String phone;             // 手机号（可选）

    private String address;           // 地址（可选）
}
