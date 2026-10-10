package com.southwind.aispringboot.common;

public enum ResultCode {
    SUCCESS("200", "操作成功", 200),
    ERROR("-1", "操作失败", 500),
    UNAUTHORIZED("401", "暂未登录或token已经过期", 401),
    SYSTEM_ERROR("500", "系统错误", 500),

    PARAM_ERROR("400", "参数错误", 400),
    PARAM_MISSING("4001", "缺少必要参数", 400),
    PARAM_INVALID("4002", "参数格式不正确", 400),

    FILE_NOT_FOUND("5001", "文件不存在", 404),
    FILE_UPLOAD_FAILED("5002", "文件上传失败", 500),
    FILE_DELETE_FAILED("5003", "文件删除失败", 500),
    FILE_SIZE_EXCEEDED("5004", "文件大小超过限制", 400),
    FILE_TYPE_NOT_SUPPORTED("5005", "不支持的文件类型", 400),
    FILE_NAME_INVALID("5006", "文件名不合法", 400),
    FILE_CONTENT_INVALID("5007", "文件内容不合法", 400),
    FILE_SAVE_FAILED("5008", "文件保存失败", 500),

    BUSINESS_ERROR("6000", "业务处理失败", 500),
    ACCOUNT_SAME("6001", "用户名已存在", 409),
    USER_NOT_EXIST("6002", "用户不存在", 404),

    TOKEN_INVALID("A0230", "token无效", 401),
    TOKEN_EXPIRED("A0230", "token已过期", 401),
    TOKEN_BLOCKED("A0230", "token已加入黑名单", 401),
    TOKEN_ACCESS_FORBIDDEN("A0231", "token已被禁止访问", 403),
    AUTHORIZED_ERROR("A0300", "访问权限异常", 403),
    ACCESS_UNAUTHORIZED("A0301", "访问未授权", 401);

    private String code;
    private String msg;
    private int httpStatus;

    ResultCode(String code, String msg, int httpStatus) {
        this.code = code;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
