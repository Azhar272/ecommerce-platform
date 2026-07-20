package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.CartItem;
import com.ecommerce.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车管理接口
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取当前用户的购物车列表
     * GET /api/cart
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getCart(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(cartService.getCartList(userId));
    }

    /**
     * 添加商品到购物车
     * POST /api/cart
     * 参数：{ productId: 1, quantity: 2 }
     */
    @PostMapping
    public Result<CartItem> addToCart(@RequestBody Map<String, Integer> body,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer productId = body.get("productId");
        Integer quantity = body.getOrDefault("quantity", 1);

        CartItem item = cartService.addToCart(userId, productId, quantity);
        return Result.success("已添加到购物车", item);
    }

    /**
     * 更新购物车项数量
     * PUT /api/cart/{id}
     * 参数：{ quantity: 3 }
     */
    @PutMapping("/{id}")
    public Result<CartItem> updateQuantity(@PathVariable Integer id,
                                            @RequestBody Map<String, Object> body) {
        Integer quantity = (Integer) body.get("quantity");
        Boolean selected = body.containsKey("selected") ? (Boolean) body.get("selected") : null;

        if (quantity != null) {
            CartItem item = cartService.updateQuantity(id, quantity);
            return Result.success(item);
        }
        if (selected != null) {
            CartItem item = cartService.updateSelected(id, selected);
            return Result.success(item);
        }
        throw new RuntimeException("请提供 quantity 或 selected 参数");
    }

    /**
     * 删除购物车项
     * DELETE /api/cart/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> removeItem(@PathVariable Integer id) {
        cartService.removeById(id);
        return Result.success("已从购物车移除", null);
    }
}
