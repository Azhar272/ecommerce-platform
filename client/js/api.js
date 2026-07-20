/**
 * API 请求工具
 * 封装所有后端接口调用，统一管理
 */
const BASE_URL = 'http://localhost:8080/api';
const TOKEN_KEY = 'ecommerce_token';
const USER_KEY = 'ecommerce_user';

// ==================== HTTP 请求封装 ====================

/** 通用请求方法，自动带 Token */
async function request(url, options = {}) {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const res = await fetch(BASE_URL + url, { ...options, headers });
    const data = await res.json();
    if (data.code === 401) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        location.href = 'login.html';
        return null;
    }
    return data;
}

function get(url)           { return request(url); }
function post(url, body)    { return request(url, { method: 'POST', body: JSON.stringify(body) }); }
function put(url, body)     { return request(url, { method: 'PUT',  body: JSON.stringify(body) }); }
function del(url)           { return request(url, { method: 'DELETE' }); }

// ==================== 用户相关 ====================

function getCurrentUser() {
    const u = localStorage.getItem(USER_KEY);
    return u ? JSON.parse(u) : null;
}

function isLoggedIn() {
    return !!localStorage.getItem(TOKEN_KEY);
}

function isAdmin() {
    const u = getCurrentUser();
    return u && u.role === 'ADMIN';
}

/** 退出登录 */
function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    location.href = 'index.html';
}

// ==================== API 接口 ====================

// 用户
const userApi = {
    login:     (body) => post('/users/login', body),
    register:  (body) => post('/users/register', body),
    getMe:     ()    => get('/users/me'),
    list:      ()    => get('/users'),
    delete:    (id)  => del('/users/' + id),
};

// 分类
const categoryApi = {
    tree:      ()    => get('/categories'),
    create:    (body) => post('/categories', body),
    update:    (id, body) => put('/categories/' + id, body),
    delete:    (id)  => del('/categories/' + id),
};

// 商品
const productApi = {
    list:      (params = {}) => {
        const q = new URLSearchParams(params).toString();
        return get('/products?' + q);
    },
    detail:    (id)  => get('/products/' + id),
    create:    (body) => post('/products', body),
    update:    (id, body) => put('/products/' + id, body),
    delete:    (id)  => del('/products/' + id),
};

// 购物车
const cartApi = {
    list:      ()    => get('/cart'),
    add:       (body) => post('/cart', body),
    update:    (id, body) => put('/cart/' + id, body),
    remove:    (id)  => del('/cart/' + id),
};

// 订单
const orderApi = {
    place:     (body) => post('/orders', body),
    myOrders:  (page = 1, size = 10) => get('/orders?page=' + page + '&size=' + size),
    detail:    (id)  => get('/orders/' + id),
    cancel:    (id)  => put('/orders/' + id + '/cancel'),
    allOrders: (page = 1, size = 10) => get('/orders/admin/all?page=' + page + '&size=' + size),
    updateStatus: (id, status) => put('/orders/' + id + '/status', { status }),
};

// 评价
const reviewApi = {
    list:      (productId) => get('/products/' + productId + '/reviews'),
    submit:    (body) => post('/reviews', body),
};
