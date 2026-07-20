package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Category;
import com.ecommerce.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品分类管理接口
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类树（用于导航栏展示）
     * GET /api/categories
     * 无需登录
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 管理员：新增分类
     * POST /api/categories
     */
    @PostMapping
    public Result<Category> create(@RequestBody Category category, HttpServletRequest request) {
        checkAdmin(request);
        categoryService.save(category);
        return Result.success("创建成功", category);
    }

    /**
     * 管理员：更新分类
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Integer id, @RequestBody Category category,
                                    HttpServletRequest request) {
        checkAdmin(request);
        category.setId(id);
        categoryService.updateById(category);
        return Result.success("更新成功", category);
    }

    /**
     * 管理员：删除分类
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        categoryService.removeById(id);
        return Result.success("删除成功", null);
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限，仅管理员可操作");
        }
    }
}
