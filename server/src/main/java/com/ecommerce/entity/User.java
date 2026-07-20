package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库的 user 表
 */
@Data                       // Lombok：自动生成 getter/setter/toString
@NoArgsConstructor          // 无参构造方法
@AllArgsConstructor         // 全参构造方法
@TableName("user")          // MyBatis-Plus：指定对应的数据库表名
public class User {

    @TableId(type = IdType.AUTO)               // 主键，自增
    private Integer id;

    private String username;                   // 用户名

    private String password;                   // 密码

    private String email;                      // 邮箱

    private String phone;                      // 手机号

    private String address;                    // 地址

    private String role;                       // 角色：ADMIN 或 CUSTOMER

    @TableField(fill = FieldFill.INSERT)       // 插入时自动填充
    private LocalDateTime createdAt;           // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)// 插入和更新时自动填充
    private LocalDateTime updatedAt;           // 更新时间
}
