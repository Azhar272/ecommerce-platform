package com.ecommerce.common;

/**
 * 统一返回结果类
 * 所有接口都返回这个格式：{ code: 200, message: "操作成功", data: {...} }
 *
 * @param <T> 返回数据的类型
 */
public class Result<T> {

    private int code;       // 状态码：200 成功，其他为错误
    private String message; // 提示信息
    private T data;         // 返回的数据

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /* ---- 静态工厂方法 ---- */

    /** 操作成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 操作成功，自定义提示信息 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 操作失败（指定错误码） */
    public static Result<?> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 操作失败，默认错误码 500 */
    public static Result<?> error(String message) {
        return new Result<>(500, message, null);
    }
}
