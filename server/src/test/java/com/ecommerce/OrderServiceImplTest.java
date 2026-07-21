package com.ecommerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.service.CartService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单服务 - 单元测试
 * 验证下单、取消、状态变更等核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务 OrderServiceImpl")
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderItemMapper orderItemMapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // 手动注入 Mock：MyBatis-Plus ServiceImpl 的 baseMapper 在父类中
        orderService = new OrderServiceImpl();
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        ReflectionTestUtils.setField(orderService, "cartService", cartService);
        ReflectionTestUtils.setField(orderService, "productService", productService);
        ReflectionTestUtils.setField(orderService, "orderItemMapper", orderItemMapper);
    }

    // ==================== 下单测试 ====================

    @Nested
    @DisplayName("下单 placeOrder()")
    class PlaceOrderTests {

        private List<Map<String, Object>> cartList;
        private Product mockProduct;

        @BeforeEach
        void setUp() {
            mockProduct = new Product();
            mockProduct.setId(1);
            mockProduct.setName("iPhone 15 Pro Max");
            mockProduct.setPrice(new BigDecimal("9999.00"));
            mockProduct.setStock(50);
            mockProduct.setStatus("ON_SALE");
            mockProduct.setSalesCount(120);

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", 1);
            cartItem.put("productId", 1);
            cartItem.put("productName", "iPhone 15 Pro Max");
            cartItem.put("quantity", 1);
            cartItem.put("selected", true);
            cartList = List.of(cartItem);
        }

        @Test
        @DisplayName("正常下单 → 创建订单并扣减库存")
        void shouldPlaceOrderSuccessfully() {
            // Given
            when(cartService.getCartList(3)).thenReturn(cartList);
            when(productService.getById(1)).thenReturn(mockProduct);
            when(orderMapper.insert(any(Order.class))).thenReturn(1);
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(productService.updateById(any(Product.class))).thenReturn(true);
            when(cartService.removeById(1)).thenReturn(true);

            // When
            Map<String, Object> result = orderService.placeOrder(
                    3, "张三", "13900000001",
                    "广州市天河区体育西路128号", "请放快递柜");

            // Then
            assertNotNull(result);
            assertNotNull(result.get("orderNo"));
            assertEquals(new BigDecimal("9999.00"), result.get("totalAmount"));
            assertEquals("PENDING", result.get("status"));

            // 验证订单已保存
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderMapper).insert(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(3, savedOrder.getUserId());
            assertEquals("PENDING", savedOrder.getStatus());
            assertEquals("张三", savedOrder.getShippingName());
            assertEquals(new BigDecimal("9999.00"), savedOrder.getTotalAmount());

            // 验证库存已扣减
            verify(productService).updateById(any(Product.class));
            // 验证购物车已清空
            verify(cartService).removeById(1);
        }

        @Test
        @DisplayName("购物车无勾选商品 → 抛出异常")
        void shouldRejectEmptyCart() {
            // Given: 没有勾选的商品
            Map<String, Object> unselected = new HashMap<>();
            unselected.put("id", 1);
            unselected.put("productId", 1);
            unselected.put("selected", false);
            when(cartService.getCartList(3)).thenReturn(List.of(unselected));

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.placeOrder(3, "张三", "13800138000",
                            "地址", null));

            assertEquals("购物车中没有勾选的商品", ex.getMessage());
            verify(orderMapper, never()).insert(any(Order.class));
        }

        @Test
        @DisplayName("商品已下架 → 抛出异常")
        void shouldRejectOffSaleProduct() {
            mockProduct.setStatus("OFF_SALE");
            when(cartService.getCartList(3)).thenReturn(cartList);
            when(productService.getById(1)).thenReturn(mockProduct);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.placeOrder(3, "张三", "13800138000",
                            "地址", null));

            assertTrue(ex.getMessage().contains("已下架"));
            verify(orderMapper, never()).insert(any(Order.class));
        }

        @Test
        @DisplayName("库存不足 → 抛出异常")
        void shouldRejectInsufficientStock() {
            mockProduct.setStock(0);  // 库存为 0
            when(cartService.getCartList(3)).thenReturn(cartList);
            when(productService.getById(1)).thenReturn(mockProduct);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.placeOrder(3, "张三", "13800138000",
                            "地址", null));

            assertTrue(ex.getMessage().contains("库存不足"));
            verify(orderMapper, never()).insert(any(Order.class));
        }
    }

    // ==================== 取消订单测试 ====================

    @Nested
    @DisplayName("取消订单 cancelOrder()")
    class CancelOrderTests {

        private Order pendingOrder;

        @BeforeEach
        void setUp() {
            pendingOrder = new Order();
            pendingOrder.setId(1);
            pendingOrder.setOrderNo("ORD202607210001");
            pendingOrder.setUserId(3);
            pendingOrder.setTotalAmount(new BigDecimal("9999.00"));
            pendingOrder.setStatus("PENDING");
        }

        @Test
        @DisplayName("待支付订单 → 成功取消并恢复库存")
        void shouldCancelPendingOrder() {
            // Given
            when(orderMapper.selectById(1)).thenReturn(pendingOrder);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);

            OrderItem item = new OrderItem();
            item.setProductId(1);
            item.setQuantity(1);

            Product product = new Product();
            product.setId(1);
            product.setStock(49);
            product.setSalesCount(121);

            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(item));
            when(productService.getById(1)).thenReturn(product);
            when(productService.updateById(any(Product.class))).thenReturn(true);

            // When
            orderService.cancelOrder(3, 1);

            // Then: 订单状态变为 CANCELLED
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderMapper).updateById(captor.capture());
            assertEquals("CANCELLED", captor.getValue().getStatus());

            // 验证库存已恢复
            verify(productService).updateById(any(Product.class));
        }

        @Test
        @DisplayName("非本人订单 → 抛出无权限异常")
        void shouldRejectCancelByOtherUser() {
            when(orderMapper.selectById(1)).thenReturn(pendingOrder);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.cancelOrder(999, 1)); // userId 不匹配

            assertEquals("无权操作此订单", ex.getMessage());
            verify(orderMapper, never()).updateById(any(Order.class));
        }

        @Test
        @DisplayName("非待支付状态 → 抛出异常")
        void shouldRejectCancelNonPendingOrder() {
            pendingOrder.setStatus("SHIPPED");
            when(orderMapper.selectById(1)).thenReturn(pendingOrder);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.cancelOrder(3, 1));

            assertEquals("只有待支付状态的订单才能取消", ex.getMessage());
        }
    }

    // ==================== 更新订单状态测试 ====================

    @Nested
    @DisplayName("更新订单状态 updateOrderStatus()")
    class UpdateStatusTests {

        @Test
        @DisplayName("管理员更新状态 → 成功")
        void shouldUpdateStatusAsAdmin() {
            Order order = new Order();
            order.setId(1);
            order.setStatus("PENDING");
            when(orderMapper.selectById(1)).thenReturn(order);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);

            orderService.updateOrderStatus(1, "SHIPPED");

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderMapper).updateById(captor.capture());
            assertEquals("SHIPPED", captor.getValue().getStatus());
        }

        @Test
        @DisplayName("订单不存在 → 抛出异常")
        void shouldThrowWhenOrderNotFound() {
            when(orderMapper.selectById(999)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    orderService.updateOrderStatus(999, "SHIPPED"));

            assertEquals("订单不存在", ex.getMessage());
        }
    }
}
