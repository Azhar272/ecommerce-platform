package com.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 下单（从购物车已勾选商品生成订单）
     * POST /api/orders
     */
    @PostMapping
    public Result<Map<String, Object>> placeOrder(@Valid @RequestBody OrderRequest req,
                                                   HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Map<String, Object> order = orderService.placeOrder(
                userId, req.getShippingName(), req.getShippingPhone(),
                req.getShippingAddress(), req.getRemark());
        return Result.success("下单成功", order);
    }

    /**
     * 获取当前用户的订单列表
     * GET /api/orders?page=1&size=10
     */
    @GetMapping
    public Result<PageResult<Order>> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Page<Order> result = orderService.getUserOrders(userId, page, size);
        PageResult<Order> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 获取订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Integer id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 取消订单
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        orderService.cancelOrder(userId, id);
        return Result.success("订单已取消", null);
    }

    /**
     * 管理员：获取所有订单
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    public Result<PageResult<Order>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkAdmin(request);
        Page<Order> result = orderService.getAllOrders(page, size);
        PageResult<Order> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 管理员：更新订单状态
     * PUT /api/orders/{id}/status
     * 参数：{ status: "SHIPPED" }
     */
    @PutMapping("/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Integer id,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest request) {
        checkAdmin(request);
        orderService.updateOrderStatus(id, body.get("status"));
        return Result.success("状态更新成功", null);
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限，仅管理员可操作");
        }
    }
}
