package cn.linaxhua.file_transfer.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用来定义返回信息
 */
@Getter
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public enum ResponseConstant {
    //响应成功
    SUCCESS(200, "响应成功"),
    //操作失败
    FAILED(601, "操作失败"),
    //身份验证失败
    AUTH_ERROR(401, "身份验证失败"),
    //权限不足
    ACCESS_ERROR(403, "权限不足"),
    //系统错误
    SYS_ERROR(500, "系统错误"),
    //参数错误
    PARAM_ERROR(400, "参数错误"),
    //未知错误
    UNKNOWN_ERROR(499, "未知错误");

    private int code;
    private String message;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
