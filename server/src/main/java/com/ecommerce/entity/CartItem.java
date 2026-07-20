package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 购物车实体类，对应数据库的 cart_item 表
 * 用户和商品的唯一约束确保同一用户对同一商品只有一条记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;                    // 用户ID

    private Integer productId;                 // 商品ID

    private Integer quantity;                  // 数量

    private Boolean selected;                  // 是否勾选（下单时只结算勾选的商品）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
