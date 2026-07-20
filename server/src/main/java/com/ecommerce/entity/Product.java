package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类，对应数据库的 product 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;                       // 商品名称

    private String description;                // 商品描述

    private BigDecimal price;                  // 单价（DECIMAL 类型用 BigDecimal）

    private Integer stock;                     // 库存数量

    private String imageUrl;                   // 商品图片URL

    private Integer categoryId;                // 所属分类ID

    private String status;                     // 状态：ON_SALE=在售, OFF_SALE=下架

    private Integer salesCount;                // 累计销量

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
