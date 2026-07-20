package com.ecommerce.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.Order;
import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 下单：从购物车已勾选的商品生成订单
     * @param userId          用户ID
     * @param shippingName    收货人
     * @param shippingPhone   收货电话
     * @param shippingAddress 收货地址
     * @param remark          备注
     * @return 订单详情
     */
    Map<String, Object> placeOrder(Integer userId, String shippingName,
                                   String shippingPhone, String shippingAddress, String remark);

    /** 获取用户订单列表（分页） */
    Page<Order> getUserOrders(Integer userId, int page, int size);

    /** 获取订单详情（含订单明细） */
    Map<String, Object> getOrderDetail(Integer orderId);

    /** 取消订单（仅 PENDING 状态可取消） */
    void cancelOrder(Integer userId, Integer orderId);

    /** 管理员更新订单状态 */
    void updateOrderStatus(Integer orderId, String status);

    /** 管理员获取所有订单 */
    Page<Order> getAllOrders(int page, int size);
}
