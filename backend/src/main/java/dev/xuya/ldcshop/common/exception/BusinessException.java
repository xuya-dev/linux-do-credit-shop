package dev.xuya.ldcshop.common.exception;

import dev.xuya.ldcshop.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类 / Business Exception
 * 用于在业务逻辑中抛出可预期的错误，由全局异常处理器统一捕获
 *
 * @author xuya
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String errorMessage;

    /**
     * 通过结果码构造 / Construct with ResultCode
     *
     * @param resultCode 结果码 / Result code
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
    }

    /**
     * 通过自定义状态码和消息构造 / Construct with custom code and message
     *
     * @param code    状态码 / Status code
     * @param message 错误消息 / Error message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorMessage = message;
    }
}
