package com.rice.mianshi.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.rice.mianshi.common.BaseResponse;
import com.rice.mianshi.common.ErrorCode;
import com.rice.mianshi.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author <a href="https://github.com/ricejson">程序员米饭</a>
 * @from <a href="https://ricejson.icu">编程导航知识星球</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
    }

    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> notRuleExceptionHandler(NotRoleException e) {
        log.error("NotRoleException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无权限");
    }
}
