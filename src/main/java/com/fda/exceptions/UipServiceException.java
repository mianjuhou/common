package com.fda.exceptions;



/**
 * UIP 异常类
 * @author Hulunliang
 * @date 2020/2/3.
 */

public class UipServiceException extends RuntimeException {
    /**
     * 异常编码
     */
    private Integer code;

    /**
     * 异常类型
     */
    private ErrorType errorType;

    /**
     * 额外信息
     */
    private Object extraInfo;

    /**
     * 异常提示信息
     */
    //private String message;

    public UipServiceException() {

    }

    public UipServiceException(Integer code, ErrorType errorType, String message, Object extraInfo, Throwable e) {
        super(message, e);
        this.code = code;
        this.errorType = errorType;
        this.extraInfo = extraInfo;
    }

    public UipServiceException(Integer code, ErrorType errorType, String message, Object extraInfo) {
        super(message);
        this.code = code;
        this.errorType = errorType;
        this.extraInfo = extraInfo;
    }

    public UipServiceException(String message, Object extraInfo) {
        super(message);
        this.errorType = ErrorType.ERROR;
        this.extraInfo = extraInfo;
    }

    public UipServiceException(ErrorType errorType, String message, Object extraInfo) {
        super(message);
        this.errorType = errorType;
        this.extraInfo = extraInfo;
    }

    public UipServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.errorType = ErrorType.ERROR;
    }

    public UipServiceException(String message) {
        super(message);
        this.errorType = ErrorType.ERROR;
    }
    public UipServiceException(Integer code, ErrorType errorType, String message) {
        super(message);
        this.code = code;
        this.errorType = errorType;
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        /**
         * 执行操作失败
         */
        ERROR,

        /**
         * 执行操作部分成功
         */
        PARTIAL_SUCCESS
    }

    public static final int EXCEPTION_CODE_CREATE = 1001;
    public static final String EXCEPTION_MESSAGE_CREATE = "创建";

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }
}
