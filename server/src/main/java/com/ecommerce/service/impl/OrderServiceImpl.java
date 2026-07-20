package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.ecommerce.mapper.OrderItemMapper orderItemMapper;

    /**
     * 下单：从购物车中取出已勾选的商品，创建订单
     * @Transactional 注解确保整个操作是一个事务：要么全部成功，要么全部回滚
     */
    @Override
    @Transactional
    public Map<String, Object> placeOrder(Integer userId, String shippingName,
                                          String shippingPhone, String shippingAddress, String remark) {
        // 1. 获取用户购物车中已勾选的商品
        List<Map<String, Object>> cartList = cartService.getCartList(userId);
        List<Map<String, Object>> selectedItems = cartList.stream()
                .filter(item -> (Boolean) item.get("selected"))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("购物车中没有勾选的商品");
        }

        // 2. 计算总金额，同时校验库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> cartItem : selectedItems) {
            Integer productId = (Integer) cartItem.get("productId");
            Product product = productService.getById(productId);

            if (product == null || !"ON_SALE".equals(product.getStatus())) {
                throw new RuntimeException("商品【" + cartItem.get("productName") + "】已下架");
            }

            Integer quantity = (Integer) cartItem.get("quantity");
            if (product.getStock() < quantity) {
                throw new RuntimeException("商品【" + product.getName() + "】库存不足");
            }

            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(new BigDecimal(quantity));

            // 创建订单明细（价格快照）
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImageUrl());
            orderItem.setPrice(price);
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 创建订单
        String orderNo = generateOrderNo();     // 生成订单编号
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingAddress(shippingAddress);
        order.setRemark(remark);
        save(order);    // 保存订单

        // 4. 保存订单明细，同时扣减库存、增加销量
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);

            // 扣减库存
            Product product = productService.getById(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            product.setSalesCount(product.getSalesCount() + item.getQuantity());
            productService.updateById(product);
        }

        // 5. 清空购物车中已下单的商品
        for (Map<String, Object> cartItem : selectedItems) {
            cartService.removeById((Integer) cartItem.get("id"));
        }

        // 6. 返回订单信息
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", order.getTotalAmount());
        result.put("status", order.getStatus());
        result.put("createdAt", order.getCreatedAt());
        return result;
    }

    /** 生成订单编号：ORD + 日期 + 4位随机数 */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "ORD" + dateStr + random;
    }

    /** 获取用户订单列表（分页） */
    @Override
    public Page<Order> getUserOrders(Integer userId, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .orderByDesc(Order::getCreatedAt);
        Page<Order> pageResult = new Page<>(page, size);
        return page(pageResult, wrapper);
    }

    /** 获取订单详情（含订单明细） */
    @Override
    public Map<String, Object> getOrderDetail(Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    /** 取消订单 */
    @Override
    @Transactional
    public void cancelOrder(Integer userId, Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只有待支付状态的订单才能取消");
        }

        order.setStatus("CANCELLED");
        updateById(order);

        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSalesCount(product.getSalesCount() - item.getQuantity());
                productService.updateById(product);
            }
        }
    }

    /** 管理员更新订单状态 */
    @Override
    public void updateOrderStatus(Integer orderId, String status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(status);
        updateById(order);
    }

    /** 管理员获取所有订单（分页） */
    @Override
    public Page<Order> getAllOrders(int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> pageResult = new Page<>(page, size);
        return page(pageResult, wrapper);
    }
}
