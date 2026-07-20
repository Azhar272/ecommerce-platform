package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类，对应数据库的 order 表
 * 注意：order 是 MySQL 保留字，所以表名必须加反引号
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`order`")           // 反引号转义 MySQL 保留字
public class Order {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;                    // 订单编号，如 ORD202606250001

    private Integer userId;                    // 下单用户ID

    private BigDecimal totalAmount;            // 订单总金额

    private String status;                     // 状态：PENDING/PAID/SHIPPED/DELIVERED/CANCELLED

    private String shippingName;               // 收货人姓名

    private String shippingPhone;              // 收货人电话

    private String shippingAddress;            // 收货地址

    private String remark;                     // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
