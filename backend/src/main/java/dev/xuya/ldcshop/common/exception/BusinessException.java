package dev.xuya.ldcshop.common.exception;

import dev.xuya.ldcshop.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类 / Business Exception
 * 用于在业务逻辑中抛出可预期的异常
 *
 * @author xuya
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误状态码 / Error Status Code
     */
    private final int code;

    /**
     * 错误信息 / Error Message
     */
    private final String errorMessage;

    /**
     * 通过 ResultCode 构造 / Construct with ResultCode
     *
     * @param resultCode 结果码 / result code
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
    }

    /**
     * 通过自定义状态码和消息构造 / Construct with custom code and message
     *
     * @param code    状态码 / status code
     * @param message 错误信息 / error message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorMessage = message;
    }
}
