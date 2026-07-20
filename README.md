# 电商平台 - 软件设计与体系课程大作业

一个基于 **Spring Boot + MyBatis-Plus + MySQL** 的全栈电商平台，前端使用原生 HTML/CSS/JS。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.x |
| ORM | MyBatis-Plus |
| 数据库 | MySQL |
| 认证 | JWT (JSON Web Token) |
| 前端 | 原生 HTML + CSS + JavaScript |
| 构建工具 | Maven |

## 项目结构

```
├── client/                 # 前端
│   ├── index.html          # 首页
│   ├── login.html          # 登录
│   ├── register.html       # 注册
│   ├── cart.html           # 购物车
│   ├── orders.html         # 订单列表
│   ├── order-detail.html   # 订单详情
│   ├── product-detail.html # 商品详情
│   ├── admin.html          # 后台管理
│   ├── css/style.css       # 样式
│   └── js/api.js           # API 封装
├── server/                 # 后端
│   ├── src/main/java/com/ecommerce/
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 业务逻辑
│   │   ├── mapper/         # 数据访问
│   │   ├── entity/         # 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── config/         # 配置类
│   │   ├── common/         # 通用类
│   │   └── util/           # 工具类
│   └── pom.xml
└── database.sql            # 数据库建表脚本
```

## 功能模块

- 用户注册/登录（JWT 认证）
- 商品浏览、分类筛选
- 商品详情查看
- 购物车管理
- 订单创建与查看
- 商品评价
- 后台管理（商品、分类、用户）

## 快速启动

### 1. 创建数据库
```sql
-- 执行 database.sql
mysql -u root -p < database.sql
```

### 2. 配置数据库连接
修改 `server/src/main/resources/application.yml` 中的数据库用户名和密码。

### 3. 启动后端
```bash
cd server
mvn spring-boot:run
```

### 4. 打开前端
直接用浏览器打开 `client/index.html`，或使用 Live Server。

## AI 辅助开发声明

本项目使用 **Claude Code** 作为编程辅助工具。所有代码均经过本人**审查、理解和修改**。以下是本人手写与 AI 生成的边界说明：

### 本人主要负责（手写/深度修改）

| 模块 | 具体内容 | 说明 |
|------|---------|------|
| 数据库设计 | `database.sql` 全部表结构 | 独立完成 ER 图设计和建表 SQL |
| 接口设计 | Controller 层的路由规划和参数设计 | 定义了 RESTful API 规范 |
| 业务逻辑 | Service 层核心逻辑 | 手写购物车、订单等业务规则和状态流转 |
| 前端页面 | HTML 结构和 CSS 样式 | 设计页面布局和交互流程 |
| JWT 认证 | 登录拦截器和 Token 验证逻辑 | 理解并实现了认证流程 |
| 异常处理 | 全局异常处理器 | 自定义错误码和返回格式 |

### AI 辅助生成（模板化/重复性代码）

| 模块 | 具体内容 | 说明 |
|------|---------|------|
| MyBatis-Plus 配置 | 分页插件、自动填充配置 | 框架标准配置 |
| Mapper 接口 | 各实体对应的 Mapper | 继承 BaseMapper，模板代码 |
| 实体类 | Entity 层 POJO | 根据数据库表自动映射 |
| 跨域配置 | CORS 配置类 | 通用配置 |
| 前端 API 封装 | `api.js` 网络请求封装 | 通用 AJAX 封装 |

### 开发工作流

1. 本人设计数据库 ER 图和 API 接口规范
2. 使用 Claude Code 生成框架模板代码
3. 本人手写核心业务逻辑，AI 辅助补全
4. 本人逐行审查所有代码，修复 AI 生成中的错误
5. 本人进行集成测试和调试

## 运行截图

> 请在此处补充运行截图或演示视频链接

## 许可证

本项目仅用于课程学习目的。
