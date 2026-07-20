package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.Review;
import java.util.List;
import java.util.Map;

public interface ReviewService extends IService<Review> {

    /** 发表评价 */
    Review submitReview(Integer userId, Integer productId, Integer rating, String content);

    /** 获取某商品的评价列表（含用户名） */
    List<Map<String, Object>> getProductReviews(Integer productId);
}
