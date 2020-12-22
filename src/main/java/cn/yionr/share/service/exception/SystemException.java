package cn.yionr.share.service.exception;

public class SystemException extends RuntimeException{
    private String code;//状态码
    public SystemException(String message, String code) {
        super(message);
        this.code = code;
    }
    public String getCode() {
        return code;
    }
}