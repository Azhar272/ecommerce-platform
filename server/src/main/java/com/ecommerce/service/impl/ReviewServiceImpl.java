package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private UserServiceImpl userService;

    /**
     * 发表评价
     * 同一用户对同一商品只能评价一次
     */
    @Override
    public Review submitReview(Integer userId, Integer productId, Integer rating, String content) {
        // 检查是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
               .eq(Review::getProductId, productId);
        Review existReview = getOne(wrapper);
        if (existReview != null) {
            throw new RuntimeException("您已经对该商品进行过评价");
        }

        // 创建评价
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setContent(content);
        save(review);
        return review;
    }

    /**
     * 获取某商品的评价列表（含用户名）
     */
    @Override
    public List<Map<String, Object>> getProductReviews(Integer productId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .orderByDesc(Review::getCreatedAt);
        List<Review> reviews = list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            User user = userService.getById(review.getUserId());

            Map<String, Object> map = new HashMap<>();
            map.put("id", review.getId());
            map.put("userId", review.getUserId());
            map.put("productId", review.getProductId());
            map.put("rating", review.getRating());
            map.put("content", review.getContent());
            map.put("createdAt", review.getCreatedAt());
            map.put("username", user != null ? user.getUsername() : "未知用户");
            result.add(map);
        }
        return result;
    }
}
