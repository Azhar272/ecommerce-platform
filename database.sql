-- ============================================================
-- 电商平台管理系统 - 完整数据库脚本
-- 使用方法：在 MySQL Workbench 中打开此文件，全选执行即可
-- 注意：如果已有 ecommerce 数据库，会先删除再重建
-- ============================================================

DROP DATABASE IF EXISTS `ecommerce`;
CREATE DATABASE `ecommerce` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ecommerce`;

-- ============================================================
-- 1. 用户表 (user)
-- ============================================================
CREATE TABLE `user` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
    `username`    VARCHAR(50)  NOT NULL UNIQUE   COMMENT '用户名，唯一，用于登录',
    `password`    VARCHAR(255) NOT NULL          COMMENT '密码',
    `email`       VARCHAR(100) DEFAULT NULL      COMMENT '电子邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL      COMMENT '手机号码',
    `address`     VARCHAR(255) DEFAULT NULL      COMMENT '收货地址',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' COMMENT '角色：ADMIN=管理员, CUSTOMER=普通用户',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 商品分类表 (category) - 支持多级分类，通过 parent_id 自引用
-- ============================================================
CREATE TABLE `category` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID，主键自增',
    `name`        VARCHAR(50)  NOT NULL UNIQUE   COMMENT '分类名称',
    `description` VARCHAR(255) DEFAULT NULL      COMMENT '分类描述',
    `parent_id`   INT          DEFAULT NULL      COMMENT '父分类ID，NULL 表示顶级分类',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`parent_id`) REFERENCES `category`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================================
-- 3. 商品表 (product)
-- ============================================================
CREATE TABLE `product` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID，主键自增',
    `name`        VARCHAR(100)   NOT NULL       COMMENT '商品名称',
    `description` TEXT           DEFAULT NULL   COMMENT '商品描述',
    `price`       DECIMAL(10,2)  NOT NULL       COMMENT '商品单价',
    `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    `image_url`   VARCHAR(500)   DEFAULT NULL   COMMENT '商品图片URL',
    `category_id` INT            DEFAULT NULL   COMMENT '所属分类ID',
    `status`      VARCHAR(20)    NOT NULL DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE=在售, OFF_SALE=下架',
    `sales_count` INT            NOT NULL DEFAULT 0 COMMENT '累计销量',
    `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 4. 购物车表 (cart_item)
-- ============================================================
CREATE TABLE `cart_item` (
    `id`         INT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    `user_id`    INT       NOT NULL              COMMENT '用户ID',
    `product_id` INT       NOT NULL              COMMENT '商品ID',
    `quantity`   INT       NOT NULL DEFAULT 1    COMMENT '数量',
    `selected`   TINYINT(1) NOT NULL DEFAULT 1   COMMENT '是否勾选：1=选中, 0=未选中',
    `created_at` DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    FOREIGN KEY (`user_id`)    REFERENCES `user`(`id`)    ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================================
-- 5. 订单表 (`order` - 反引号是因为 order 是 MySQL 保留字)
-- ============================================================
CREATE TABLE `order` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    `order_no`         VARCHAR(30)    NOT NULL UNIQUE COMMENT '订单编号',
    `user_id`          INT            NOT NULL              COMMENT '下单用户ID',
    `total_amount`     DECIMAL(10,2)  NOT NULL              COMMENT '订单总金额',
    `status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PAID/SHIPPED/DELIVERED/CANCELLED',
    `shipping_name`    VARCHAR(50)    DEFAULT NULL          COMMENT '收货人姓名',
    `shipping_phone`   VARCHAR(20)    DEFAULT NULL          COMMENT '收货人电话',
    `shipping_address` VARCHAR(255)   DEFAULT NULL          COMMENT '收货地址',
    `remark`           VARCHAR(500)   DEFAULT NULL          COMMENT '用户备注',
    `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 6. 订单明细表 (order_item) - 价格快照设计
-- ============================================================
CREATE TABLE `order_item` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单明细ID',
    `order_id`      INT            NOT NULL              COMMENT '所属订单ID',
    `product_id`    INT            NOT NULL              COMMENT '商品ID',
    `product_name`  VARCHAR(100)   NOT NULL              COMMENT '商品名称快照',
    `product_image` VARCHAR(500)   DEFAULT NULL          COMMENT '商品图片快照',
    `price`         DECIMAL(10,2)  NOT NULL              COMMENT '下单时的商品单价快照',
    `quantity`      INT            NOT NULL              COMMENT '购买数量',
    `subtotal`      DECIMAL(10,2)  NOT NULL              COMMENT '小计 = price × quantity',
    FOREIGN KEY (`order_id`)   REFERENCES `order`(`id`)   ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================================
-- 7. 商品评价表 (review)
-- ============================================================
CREATE TABLE `review` (
    `id`         INT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    `user_id`    INT          NOT NULL                COMMENT '评价用户ID',
    `product_id` INT          NOT NULL                COMMENT '被评价商品ID',
    `rating`     TINYINT      NOT NULL DEFAULT 5      COMMENT '评分：1-5',
    `content`    TEXT         DEFAULT NULL            COMMENT '评价内容',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    FOREIGN KEY (`user_id`)    REFERENCES `user`(`id`)    ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_user_product_review` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';


-- ============================================================
-- 插入示例数据
-- ============================================================

-- 1. 用户数据（密码均为 123456，实际项目需加密）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `address`, `role`) VALUES
('admin',      '123456', 'admin@ecommerce.com',   '13800000001', '北京市朝阳区',              'ADMIN'),
('admin2',     '123456', 'admin2@ecommerce.com',  '13800000002', '上海市浦东新区',            'ADMIN'),
('zhangsan',   '123456', 'zhangsan@qq.com',       '13900000001', '广州市天河区体育西路128号',  'CUSTOMER'),
('lisi',       '123456', 'lisi@163.com',          '13900000002', '深圳市南山区科技园路56号',   'CUSTOMER'),
('wangwu',     '123456', 'wangwu@gmail.com',      '13900000003', '杭州市西湖区文三路100号',    'CUSTOMER');

-- 2. 商品分类（两级结构）
INSERT INTO `category` (`id`, `name`, `description`, `parent_id`) VALUES
(1,  '电子产品', '手机、电脑、配件等', NULL),
(2,  '服装',     '男装、女装、鞋帽等', NULL),
(3,  '食品饮料', '零食、饮品、保健品等', NULL),
(4,  '手机',     '智能手机及配件',     1),
(5,  '电脑',     '笔记本和台式机',     1),
(6,  '耳机',     '有线及无线耳机',     1),
(7,  '男装',     '男士服装',           2),
(8,  '女装',     '女士服装',           2),
(9,  '零食',     '休闲零食',           3),
(10, '饮料',     '各类饮品',           3);

-- 3. 商品数据
INSERT INTO `product` (`name`, `description`, `price`, `stock`, `image_url`, `category_id`, `status`, `sales_count`) VALUES
('iPhone 15 Pro Max', 'Apple 最新旗舰手机，A17 Pro 芯片，钛金属边框', 9999.00, 50, NULL, 4, 'ON_SALE', 120),
('华为 Mate 60 Pro',  '华为旗舰手机，麒麟芯片，卫星通信',           6999.00, 30, NULL, 4, 'ON_SALE', 85),
('小米14 Ultra',      '小米旗舰，骁龙8Gen3，徕卡光学镜头',           5999.00, 80, NULL, 4, 'ON_SALE', 200),
('MacBook Pro 14',    'Apple M3 Pro芯片，14英寸Liquid Retina显示屏',14999.00,20, NULL, 5, 'ON_SALE', 55),
('联想 ThinkPad X1',  '商务旗舰笔记本，i7处理器，32GB内存',         8999.00, 15, NULL, 5, 'ON_SALE', 40),
('AirPods Pro 2',     'Apple 主动降噪无线耳机，H2芯片',             1899.00, 100,NULL, 6, 'ON_SALE', 300),
('索尼 WH-1000XM5',   '索尼旗舰降噪头戴耳机，30小时续航',            2499.00, 40, NULL, 6, 'ON_SALE', 90),
('商务休闲夹克',      '春秋款男士立领夹克，修身版型',               399.00,  200,NULL, 7, 'ON_SALE', 500),
('纯棉圆领T恤',       '100%新疆长绒棉，亲肤透气',                   99.00,   500,NULL, 7, 'ON_SALE', 1200),
('碎花连衣裙',        '2024夏季新款，法式复古碎花',                  299.00,  150,NULL, 8, 'ON_SALE', 350),
('高腰阔腿裤',        '垂感面料，显高显瘦，通勤百搭',                199.00,  180,NULL, 8, 'ON_SALE', 280),
('三只松鼠坚果礼盒',  '每日坚果混合装，750g大包装',                  89.00,   300,NULL, 9, 'ON_SALE', 2000),
('良品铺子牛肉干',    '麻辣味牛肉干，独立小包装，500g',              49.90,   400,NULL, 9, 'ON_SALE', 1500),
('农夫山泉矿泉水',    '天然弱碱性水，550ml×24瓶整箱',                29.90,   1000,NULL,10, 'ON_SALE', 5000),
('伊利纯牛奶',        '无菌砖纯牛奶，250ml×16盒',                    49.90,   500,NULL, 10, 'ON_SALE', 3000);

-- 4. 购物车数据（zhangsan 的购物车）
INSERT INTO `cart_item` (`user_id`, `product_id`, `quantity`, `selected`) VALUES
(3, 1,  1, 1),
(3, 6,  2, 1),
(3, 12, 3, 0);

-- 5. 订单数据
INSERT INTO `order` (`order_no`, `user_id`, `total_amount`, `status`, `shipping_name`, `shipping_phone`, `shipping_address`, `remark`) VALUES
('ORD202606250001', 3, 13797.00, 'DELIVERED', '张三', '13900000001', '广州市天河区体育西路128号', '请放快递柜'),
('ORD202606250002', 4, 15498.00, 'SHIPPED',  '李四', '13900000002', '深圳市南山区科技园路56号', NULL);

-- 6. 订单明细
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`) VALUES
(1, 1, 'iPhone 15 Pro Max', NULL, 9999.00, 1, 9999.00),
(1, 6, 'AirPods Pro 2',     NULL, 1899.00, 2, 3798.00),
(2, 4, 'MacBook Pro 14',    NULL, 14999.00,1, 14999.00),
(2, 9, '纯棉圆领T恤',       NULL, 99.00,   5, 495.00);

-- 7. 评价数据
INSERT INTO `review` (`user_id`, `product_id`, `rating`, `content`) VALUES
(3, 1, 5, '非常棒的手机！运行速度飞快，拍照效果惊艳。'),
(3, 6, 4, '降噪效果很好，就是价格有点贵。'),
(4, 4, 5, 'MacBook 一如既往地好，屏幕素质太棒了。'),
(4, 9, 5, '面料很舒服，透气性好，性价比超高。');
