package com.ecommerce;

import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.service.impl.ReviewServiceImpl;
import com.ecommerce.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评价服务 - 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价服务 ReviewServiceImpl")
class ReviewServiceImplTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserServiceImpl userService;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        // 手动注入 Mock：MyBatis-Plus ServiceImpl 的 baseMapper 在父类中
        reviewService = new ReviewServiceImpl();
        ReflectionTestUtils.setField(reviewService, "baseMapper", reviewMapper);
        ReflectionTestUtils.setField(reviewService, "userService", userService);
    }

    @Nested
    @DisplayName("发表评价 submitReview()")
    class SubmitReviewTests {

        @Test
        @DisplayName("首次评价 → 成功保存")
        void shouldSubmitReviewSuccessfully() {
            when(reviewMapper.selectOne(any(), anyBoolean())).thenReturn(null);
            when(reviewMapper.insert(any(Review.class))).thenReturn(1);

            Review result = reviewService.submitReview(3, 1, 5, "很好用！");

            assertNotNull(result);
            assertEquals(3, result.getUserId());
            assertEquals(1, result.getProductId());
            assertEquals(5, result.getRating());
            assertEquals("很好用！", result.getContent());
            verify(reviewMapper).insert(any(Review.class));
        }

        @Test
        @DisplayName("重复评价 → 抛出异常")
        void shouldRejectDuplicateReview() {
            Review existing = new Review();
            existing.setId(1);
            when(reviewMapper.selectOne(any(), anyBoolean())).thenReturn(existing);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    reviewService.submitReview(3, 1, 4, "再评一次"));

            assertEquals("您已经对该商品进行过评价", ex.getMessage());
            verify(reviewMapper, never()).insert(any(Review.class));
        }
    }

    @Nested
    @DisplayName("获取评价 getProductReviews()")
    class GetProductReviewsTests {

        @Test
        @DisplayName("获取评价列表 → 包含用户名")
        void shouldReturnReviewsWithUsername() {
            Review review = new Review();
            review.setId(1);
            review.setUserId(3);
            review.setProductId(1);
            review.setRating(5);
            review.setContent("好评");

            User user = new User();
            user.setId(3);
            user.setUsername("zhangsan");

            when(reviewMapper.selectList(any())).thenReturn(List.of(review));
            when(userService.getById(3)).thenReturn(user);

            List<Map<String, Object>> result = reviewService.getProductReviews(1);

            assertEquals(1, result.size());
            assertEquals("zhangsan", result.get(0).get("username"));
            assertEquals(5, result.get(0).get("rating"));
            assertEquals("好评", result.get(0).get("content"));
        }
    }
}
