package com.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理接口
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     * GET /api/products?page=1&size=10&keyword=iPhone&categoryId=4
     * 无需登录
     */
    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId) {

        Page<Product> result = productService.pageQuery(page, size, keyword, categoryId);
        PageResult<Product> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 获取商品详情
     * GET /api/products/{id}
     * 无需登录
     */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Integer id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 管理员：新增商品
     * POST /api/products
     */
    @PostMapping
    public Result<Product> create(@RequestBody Product product, HttpServletRequest request) {
        checkAdmin(request);
        product.setSalesCount(0);
        product.setStatus("ON_SALE");
        productService.save(product);
        return Result.success("添加成功", product);
    }

    /**
     * 管理员：更新商品
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Integer id, @RequestBody Product product,
                                   HttpServletRequest request) {
        checkAdmin(request);
        product.setId(id);
        productService.updateById(product);
        return Result.success("更新成功", productService.getById(id));
    }

    /**
     * 管理员：删除商品
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        productService.removeById(id);
        return Result.success("删除成功", null);
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限，仅管理员可操作");
        }
    }
}
