package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    @Autowired
    private ProductServiceImpl productService;

    /**
     * 添加商品到购物车
     * 如果该用户购物车中已有此商品，则增加数量；
     * 如果没有，则新增一条记录
     */
    @Override
    public CartItem addToCart(Integer userId, Integer productId, Integer quantity) {
        // 1. 检查商品是否存在且在售
        Product product = productService.getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"ON_SALE".equals(product.getStatus())) {
            throw new RuntimeException("商品已下架");
        }
        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + product.getStock());
        }

        // 2. 查询购物车中是否已有该商品
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
               .eq(CartItem::getProductId, productId);
        CartItem existItem = getOne(wrapper);

        if (existItem != null) {
            // 已有：增加数量
            existItem.setQuantity(existItem.getQuantity() + quantity);
            updateById(existItem);
            return existItem;
        } else {
            // 没有：新增
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setSelected(true);   // 默认勾选
            save(cartItem);
            return cartItem;
        }
    }

    /**
     * 获取用户购物车列表
     * 关联查询商品信息，返回给前端展示
     */
    @Override
    public List<Map<String, Object>> getCartList(Integer userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
               .orderByDesc(CartItem::getCreatedAt);
        List<CartItem> items = list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = productService.getById(item.getProductId());

            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("userId", item.getUserId());
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            map.put("selected", item.getSelected());
            map.put("createdAt", item.getCreatedAt());
            // 商品信息（实时从 product 表读取，确保价格是最新的）
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productImage", product.getImageUrl());
                map.put("price", product.getPrice());
                map.put("stock", product.getStock());
                map.put("subtotal", product.getPrice().multiply(
                        new java.math.BigDecimal(item.getQuantity())));
            }
            result.add(map);
        }
        return result;
    }

    /** 更新购物车项数量 */
    @Override
    public CartItem updateQuantity(Integer cartItemId, Integer quantity) {
        CartItem item = getById(cartItemId);
        if (item == null) {
            throw new RuntimeException("购物车项不存在");
        }
        if (quantity <= 0) {
            // 数量 <= 0 则删除
            removeById(cartItemId);
            return null;
        }
        item.setQuantity(quantity);
        updateById(item);
        return item;
    }

    /** 更新勾选状态 */
    @Override
    public CartItem updateSelected(Integer cartItemId, Boolean selected) {
        CartItem item = getById(cartItemId);
        if (item == null) {
            throw new RuntimeException("购物车项不存在");
        }
        item.setSelected(selected);
        updateById(item);
        return item;
    }
}
