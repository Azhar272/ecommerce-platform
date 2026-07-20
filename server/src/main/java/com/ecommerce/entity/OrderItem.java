package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 订单明细实体类，对应数据库的 order_item 表
 * 价格字段是下单时的快照，即使后续商品改价也不影响历史订单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;                   // 所属订单ID

    private Integer productId;                 // 商品ID

    private String productName;                // 商品名称快照

    private String productImage;               // 商品图片快照

    private BigDecimal price;                  // 下单时的单价快照

    private Integer quantity;                  // 购买数量

    private BigDecimal subtotal;               // 小计 = price × quantity
}
