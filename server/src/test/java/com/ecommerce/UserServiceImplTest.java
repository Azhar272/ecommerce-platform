package com.ecommerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.impl.UserServiceImpl;
import com.ecommerce.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务 - 单元测试
 * 使用 Mockito 模拟数据库层，验证业务逻辑正确性
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务 UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // 手动注入 Mock：MyBatis-Plus ServiceImpl 的 baseMapper 在父类中
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "jwtUtil", jwtUtil);
    }

    // ==================== 注册测试 ====================

    @Nested
    @DisplayName("用户注册 register()")
    class RegisterTests {

        @Test
        @DisplayName("正常注册 → 保存用户信息，默认角色为 CUSTOMER")
        void shouldRegisterSuccessfully() {
            // Given: 用户名不存在
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // When: 调用注册
            User result = userService.register("newuser", "pass123",
                    "new@test.com", "13800138000", "北京市");

            // Then: 注册成功
            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("CUSTOMER", result.getRole());
            assertEquals("new@test.com", result.getEmail());

            // 验证密码已设置
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(captor.capture());
            assertEquals("pass123", captor.getValue().getPassword());
        }

        @Test
        @DisplayName("重复用户名 → 抛出 RuntimeException")
        void shouldRejectDuplicateUsername() {
            // Given: 用户名已存在
            User existingUser = new User();
            existingUser.setUsername("zhangsan");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(existingUser);

            // When & Then: 抛出异常
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userService.register("zhangsan", "pass123",
                            "test@test.com", null, null));

            assertEquals("用户名已存在", ex.getMessage());
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    // ==================== 登录测试 ====================

    @Nested
    @DisplayName("用户登录 login()")
    class LoginTests {

        private User mockUser;

        @BeforeEach
        void setUp() {
            mockUser = new User();
            mockUser.setId(1);
            mockUser.setUsername("zhangsan");
            mockUser.setPassword("123456");
            mockUser.setRole("CUSTOMER");
        }

        @Test
        @DisplayName("正确用户名密码 → 返回 JWT Token")
        void shouldReturnTokenOnCorrectCredentials() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(mockUser);
            when(jwtUtil.generateToken(1, "zhangsan", "CUSTOMER"))
                    .thenReturn("eyJhbGciOiJIUzI1NiJ9.mocktoken");

            String token = userService.login("zhangsan", "123456");

            assertNotNull(token);
            assertTrue(token.length() > 10);
            verify(jwtUtil).generateToken(1, "zhangsan", "CUSTOMER");
        }

        @Test
        @DisplayName("密码错误 → 抛出 RuntimeException")
        void shouldRejectWrongPassword() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(mockUser);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userService.login("zhangsan", "wrongpassword"));

            assertEquals("用户名或密码错误", ex.getMessage());
            verify(jwtUtil, never()).generateToken(anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("用户不存在 → 抛出 RuntimeException")
        void shouldRejectNonexistentUser() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userService.login("nobody", "123456"));

            assertEquals("用户名或密码错误", ex.getMessage());
        }
    }

    // ==================== 查找用户测试 ====================

    @Nested
    @DisplayName("查找用户 findByUsername()")
    class FindByUsernameTests {

        @Test
        @DisplayName("存在的用户名 → 返回用户对象")
        void shouldFindExistingUser() {
            User user = new User();
            user.setUsername("zhangsan");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(user);

            User result = userService.findByUsername("zhangsan");

            assertNotNull(result);
            assertEquals("zhangsan", result.getUsername());
        }

        @Test
        @DisplayName("不存在的用户名 → 返回 null")
        void shouldReturnNullForNonexistentUser() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

            User result = userService.findByUsername("nobody");

            assertNull(result);
        }
    }
}
