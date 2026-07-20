package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.Product;

public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     * @param page       当前页码
     * @param size       每页数量
     * @param keyword    搜索关键词（模糊匹配商品名）
     * @param categoryId 分类ID（可选，null 表示查全部）
     */
    Page<Product> pageQuery(int page, int size, String keyword, Integer categoryId);
}
