package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Review;
import com.ecommerce.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品评价接口
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 获取某商品的评价列表
     * GET /api/products/{productId}/reviews
     * 无需登录
     */
    @GetMapping("/products/{productId}/reviews")
    public Result<List<Map<String, Object>>> getProductReviews(@PathVariable Integer productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }

    /**
     * 发表评价
     * POST /api/reviews
     * 参数：{ productId: 1, rating: 5, content: "很好用" }
     */
    @PostMapping("/reviews")
    public Result<Review> submitReview(@RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer productId = (Integer) body.get("productId");
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");

        Review review = reviewService.submitReview(userId, productId, rating, content);
        return Result.success("评价成功", review);
    }
}
