package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.CartItem;
import java.util.List;
import java.util.Map;

public interface CartService extends IService<CartItem> {

    /** 添加商品到购物车，如果已存在则增加数量 */
    CartItem addToCart(Integer userId, Integer productId, Integer quantity);

    /** 获取用户购物车列表（含商品详情） */
    List<Map<String, Object>> getCartList(Integer userId);

    /** 更新购物车项数量 */
    CartItem updateQuantity(Integer cartItemId, Integer quantity);

    /** 更新勾选状态 */
    CartItem updateSelected(Integer cartItemId, Boolean selected);
}
