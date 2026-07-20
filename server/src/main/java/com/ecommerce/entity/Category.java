package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 商品分类实体类，对应数据库的 category 表
 * 通过 parentId 实现多级分类（例如：电子产品 -> 手机）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;                       // 分类名称

    private String description;                // 分类描述

    private Integer parentId;                  // 父分类ID，null 表示顶级分类

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;           // 创建时间
}
