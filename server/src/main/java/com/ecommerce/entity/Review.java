package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 商品评价实体类，对应数据库的 review 表
 * 同一用户对同一商品只能评价一次（唯一约束）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;                    // 评价用户ID

    private Integer productId;                 // 被评价商品ID

    private Integer rating;                    // 评分：1-5

    private String content;                    // 评价内容

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
