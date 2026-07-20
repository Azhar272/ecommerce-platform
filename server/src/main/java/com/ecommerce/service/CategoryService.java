package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.Category;
import java.util.List;
import java.util.Map;

public interface CategoryService extends IService<Category> {

    /** 获取分类树（嵌套结构，用于前端导航栏） */
    List<Map<String, Object>> getCategoryTree();
}
