package dev.xuya.ldcshop.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 / Global Exception Handler
 * 统一捕获并处理所有异常，返回标准化的错误响应
 *
 * @author xuya
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 / Handle Business Exception
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 / Business Exception: {} - URI: {} - Message: {}",
                e.getCode(), request.getRequestURI(), e.getErrorMessage());
        return R.fail(e.getCode(), e.getErrorMessage());
    }

    /**
     * 处理 Sa-Token 未登录异常 / Handle Sa-Token Not Login Exception
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("未登录访问 / Unauthorized Access - URI: {}", request.getRequestURI());
        return R.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理 Sa-Token 无角色异常 / Handle Sa-Token No Role Exception
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.warn("角色权限不足 / Insufficient Role - URI: {} - Required Role: {}",
                request.getRequestURI(), e.getRole());
        return R.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 处理 Sa-Token 无权限异常 / Handle Sa-Token No Permission Exception
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("权限不足 / Access Denied - URI: {} - Required Permission: {}",
                request.getRequestURI(), e.getPermission());
        return R.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 处理参数校验异常 / Handle Validation Exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 / Validation Failed: {}", message);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理参数绑定异常 / Handle Bind Exception
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败 / Bind Failed: {}", message);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理非法参数异常 / Handle Illegal Argument Exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数 / Illegal Argument: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 处理所有未捕获的异常 / Handle All Uncaught Exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 / System Exception - URI: {}", request.getRequestURI(), e);
        return R.fail(ResultCode.FAIL);
    }
}
