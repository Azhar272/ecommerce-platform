package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 下单请求参数
 */
@Data
public class OrderRequest {

    @NotBlank(message = "收货人姓名不能为空")
    private String shippingName;       // 收货人姓名

    @NotBlank(message = "收货电话不能为空")
    private String shippingPhone;      // 收货电话

    @NotBlank(message = "收货地址不能为空")
    private String shippingAddress;    // 收货地址

    private String remark;             // 备注（可选）
}
