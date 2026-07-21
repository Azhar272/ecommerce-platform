package com.ecommerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.service.impl.CartServiceImpl;
import com.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 购物车服务 - 单元测试
 * 验证添加商品、更新数量、勾选切换等逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("购物车服务 CartServiceImpl")
class CartServiceImplTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductServiceImpl productService;

    private CartServiceImpl cartService;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // 手动注入 Mock：MyBatis-Plus ServiceImpl 的 baseMapper 在父类中，
        // @InjectMocks 无法注入父类字段，使用 ReflectionTestUtils 替代
        cartService = new CartServiceImpl();
        ReflectionTestUtils.setField(cartService, "baseMapper", cartItemMapper);
        ReflectionTestUtils.setField(cartService, "productService", productService);

        mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("iPhone 15 Pro Max");
        mockProduct.setPrice(new BigDecimal("9999.00"));
        mockProduct.setStock(50);
        mockProduct.setStatus("ON_SALE");
    }

    // ==================== 添加到购物车测试 ====================

    @Nested
    @DisplayName("添加购物车 addToCart()")
    class AddToCartTests {

        @Test
        @DisplayName("首次添加 → 新增购物车记录，默认勾选")
        void shouldAddNewCartItem() {
            // Given: 购物车中没有该商品
            when(productService.getById(1)).thenReturn(mockProduct);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);
            when(cartItemMapper.insert(any(CartItem.class))).thenReturn(1);

            // When
            CartItem result = cartService.addToCart(3, 1, 1);

            // Then
            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemMapper).insert(captor.capture());
            CartItem saved = captor.getValue();
            assertEquals(3, saved.getUserId());
            assertEquals(1, saved.getProductId());
            assertEquals(1, saved.getQuantity());
            assertTrue(saved.getSelected());  // 默认勾选
        }

        @Test
        @DisplayName("重复添加 → 增加已有商品数量")
        void shouldIncrementQuantityWhenAlreadyInCart() {
            // Given: 购物车中已有2个该商品
            CartItem existingItem = new CartItem();
            existingItem.setId(10);
            existingItem.setUserId(3);
            existingItem.setProductId(1);
            existingItem.setQuantity(2);
            existingItem.setSelected(true);

            when(productService.getById(1)).thenReturn(mockProduct);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(existingItem);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            // When: 再添加3个
            CartItem result = cartService.addToCart(3, 1, 3);

            // Then: 数量变为 5
            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemMapper).updateById(captor.capture());
            assertEquals(5, captor.getValue().getQuantity());
            verify(cartItemMapper, never()).insert(any(CartItem.class));
        }

        @Test
        @DisplayName("商品已下架 → 抛出异常")
        void shouldRejectOffSaleProduct() {
            mockProduct.setStatus("OFF_SALE");
            when(productService.getById(1)).thenReturn(mockProduct);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    cartService.addToCart(3, 1, 1));

            assertEquals("商品已下架", ex.getMessage());
            verify(cartItemMapper, never()).insert(any(CartItem.class));
        }

        @Test
        @DisplayName("库存不足 → 抛出异常")
        void shouldRejectWhenStockInsufficient() {
            mockProduct.setStock(0);
            when(productService.getById(1)).thenReturn(mockProduct);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    cartService.addToCart(3, 1, 1));

            assertTrue(ex.getMessage().contains("库存不足"));
            verify(cartItemMapper, never()).insert(any(CartItem.class));
        }

        @Test
        @DisplayName("商品不存在 → 抛出异常")
        void shouldRejectNonexistentProduct() {
            when(productService.getById(999)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    cartService.addToCart(3, 999, 1));

            assertEquals("商品不存在", ex.getMessage());
            verify(cartItemMapper, never()).insert(any(CartItem.class));
        }
    }

    // ==================== 更新数量测试 ====================

    @Nested
    @DisplayName("更新数量 updateQuantity()")
    class UpdateQuantityTests {

        @Test
        @DisplayName("正常更新数量")
        void shouldUpdateQuantity() {
            CartItem item = new CartItem();
            item.setId(1);
            item.setQuantity(3);

            when(cartItemMapper.selectById(1)).thenReturn(item);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            CartItem result = cartService.updateQuantity(1, 5);

            assertNotNull(result);
            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemMapper).updateById(captor.capture());
            assertEquals(5, captor.getValue().getQuantity());
        }

        @Test
        @DisplayName("数量设为0或负数 → 删除购物车项")
        void shouldDeleteWhenQuantityIsZero() {
            CartItem item = new CartItem();
            item.setId(1);

            when(cartItemMapper.selectById(1)).thenReturn(item);
            when(cartItemMapper.deleteById(1)).thenReturn(1);

            CartItem result = cartService.updateQuantity(1, 0);

            assertNull(result);
            verify(cartItemMapper).deleteById(1);
            verify(cartItemMapper, never()).updateById(any(CartItem.class));
        }
    }

    // ==================== 更新勾选状态测试 ====================

    @Nested
    @DisplayName("更新勾选状态 updateSelected()")
    class UpdateSelectedTests {

        @Test
        @DisplayName("切换勾选状态")
        void shouldToggleSelected() {
            CartItem item = new CartItem();
            item.setId(1);
            item.setSelected(true);

            when(cartItemMapper.selectById(1)).thenReturn(item);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            CartItem result = cartService.updateSelected(1, false);

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemMapper).updateById(captor.capture());
            assertFalse(captor.getValue().getSelected());
        }

        @Test
        @DisplayName("购物车项不存在 → 抛出异常")
        void shouldThrowWhenItemNotFound() {
            when(cartItemMapper.selectById(999)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    cartService.updateSelected(999, true));

            assertEquals("购物车项不存在", ex.getMessage());
        }
    }
}
