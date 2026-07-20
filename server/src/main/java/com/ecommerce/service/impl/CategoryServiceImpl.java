package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.entity.Category;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 获取分类树
     * 将所有分类组装成树形结构，方便前端渲染多级导航菜单
     * 返回格式示例：
     * [
     *   {
     *     "id": 1, "name": "电子产品",
     *     "children": [
     *       {"id": 4, "name": "手机"},
     *       {"id": 5, "name": "电脑"}
     *     ]
     *   }
     * ]
     */
    @Override
    public List<Map<String, Object>> getCategoryTree() {
        // 1. 查出所有分类
        List<Category> allCategories = list();

        // 2. 找出顶级分类（parentId == null）
        List<Map<String, Object>> tree = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("description", c.getDescription());
                    // 递归查找子分类
                    map.put("children", getChildren(c.getId(), allCategories));
                    return map;
                })
                .collect(Collectors.toList());

        return tree;
    }

    /** 递归查找某个分类下的所有子分类 */
    private List<Map<String, Object>> getChildren(Integer parentId, List<Category> all) {
        return all.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("description", c.getDescription());
                    // 递归：继续查找子分类的子分类
                    map.put("children", getChildren(c.getId(), all));
                    return map;
                })
                .collect(Collectors.toList());
    }
}
