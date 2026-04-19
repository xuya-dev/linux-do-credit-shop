package dev.xuya.ldcshop.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果 / Unified Response Result
 * 用于封装所有API接口的返回数据
 *
 * @param <T> 数据类型 / Data Type
 * @author xuya
 */
@Data
public class R<T> implements Serializable {

    /**
     * 状态码 / Status Code
     */
    private int code;

    /**
     * 提示信息 / Message
     */
    private String message;

    /**
     * 响应数据 / Response Data
     */
    private T data;

    /**
     * 时间戳 / Timestamp
     */
    private long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据） / Success response without data
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getMessage());
        return r;
    }

    /**
     * 成功响应（带数据） / Success response with data
     *
     * @param data 响应数据 / response data
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（自定义消息） / Success with custom message
     *
     * @param data    响应数据 / response data
     * @param message 提示信息 / message
     */
    public static <T> R<T> ok(T data, String message) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应 / Failure response
     *
     * @param resultCode 结果码 / result code
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setMessage(resultCode.getMessage());
        return r;
    }

    /**
     * 失败响应（自定义消息） / Failure with custom message
     *
     * @param code    状态码 / status code
     * @param message 提示信息 / message
     */
    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 判断是否成功 / Check if success
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
