package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    /**
     * 分页查询商品
     * 支持按关键词搜索商品名，按分类筛选，只查在售商品
     */
    @Override
    public Page<Product> pageQuery(int page, int size, String keyword, Integer categoryId) {
        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查在售商品
        wrapper.eq(Product::getStatus, "ON_SALE");

        // 关键词模糊搜索（匹配商品名称）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }

        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按创建时间降序排列（新品在前）
        wrapper.orderByDesc(Product::getCreatedAt);

        // 执行分页查询
        Page<Product> pageResult = new Page<>(page, size);
        return page(pageResult, wrapper);
    }
}
