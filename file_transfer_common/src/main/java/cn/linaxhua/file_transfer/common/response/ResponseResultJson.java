package cn.linaxhua.file_transfer.common.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@ToString
@Accessors(chain = true)
public class ResponseResultJson<T> implements Serializable {

    /**
     * 服务返回状态
     */
    private int code;
    /**
     * 详细状态
     */
    private int statusCode;
    /**
     * 返回状态描述
     */
    private String msg;
    /**
     * 附加信息描述
     */
    private String addtionalInfo;
    /**
     * 返回数据
     */
    private T data;

    public ResponseResultJson(ResponseConstant codeConstant, Integer statusCode, String addtionalInfo, T data) {
        this.code = codeConstant.getCode();
        if (statusCode != null) {
            this.statusCode = statusCode;
        }
        this.msg = codeConstant.getMessage();
        this.addtionalInfo = addtionalInfo;
        this.data = data;
    }

    /**
     * 正常响应
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> success(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.SUCCESS, null, msg, data);
    }

    public static <T> ResponseResultJson<T> success(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.SUCCESS, statusCode, msg, data);
    }

    /**
     * 操作失败
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> failed(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.FAILED, null, msg, data);
    }

    public static <T> ResponseResultJson<T> failed(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.FAILED, statusCode, msg, data);
    }

    /**
     * 身份验证失败
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> authError(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.AUTH_ERROR, null, msg, data);
    }

    public static <T> ResponseResultJson<T> authError(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.AUTH_ERROR, statusCode, msg, data);
    }

    /**
     * 权限不足
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> accessError(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.ACCESS_ERROR, null, msg, data);
    }

    public static <T> ResponseResultJson<T> accessError(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.ACCESS_ERROR, statusCode, msg, data);
    }

    /**
     * 系统错误
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> sysError(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.SYS_ERROR, null, msg, data);
    }

    public static <T> ResponseResultJson<T> sysError(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.SYS_ERROR, statusCode, msg, data);
    }

    /**
     * 参数错误
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> paramError(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.PARAM_ERROR, null, msg, data);
    }

    public static <T> ResponseResultJson<T> paramError(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.PARAM_ERROR, statusCode, msg, data);
    }

    /**
     * 未知错误
     *
     * @param msg  附加信息
     * @param data 返回数据
     * @return 返回结果
     */
    public static <T> ResponseResultJson<T> unknownError(String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.UNKNOWN_ERROR, null, msg, data);
    }

    public static <T> ResponseResultJson<T> unknownError(Integer statusCode, String msg, T data) {
        return new ResponseResultJson<T>(ResponseConstant.UNKNOWN_ERROR, statusCode, msg, data);
    }

    /**
     * 将返回结果转为json
     *
     * @return 返回json结果
     */
    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", this.code);
        jsonObject.put("statusCode", this.statusCode);
        jsonObject.put("msg", this.msg);
        jsonObject.put("addtionalInfo", this.addtionalInfo);
        jsonObject.put("data", this.data);
        return JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect);
    }
}
