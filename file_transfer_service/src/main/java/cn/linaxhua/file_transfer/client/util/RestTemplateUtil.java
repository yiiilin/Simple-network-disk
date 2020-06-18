package cn.linaxhua.file_transfer.client.util;

import cn.linaxhua.file_transfer.client.service.ApiService;
import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.common.entity.User;
import cn.linaxhua.file_transfer.common.response.ResponseResultJson;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class RestTemplateUtil {

    @Value("${api-service.url}")
    private String url;

    public static final String LIST_ALL_STRUCTURE = "server/busi/listAllStructure";
    public static final String ADD_STRUCTURE = "server/busi/addStructure";
    public static final String UPDATE_STRUCTURE = "server/busi/updateStructure";
    public static final String DELETE_STRUCTURE = "server/busi/deleteStructure/{id}";
    public static final String GET_STRUCTURE = "server/busi/getStructureByPath";
    public static final String DOWNLOAD_FILE = "server/busi/downloadFile";
    public static final String UPLOAD_FILE = "server/busi/uploadFile";
//    public static final String UPDATE_PASSWORD = "server/user/updatePassword?oldPassword=${oldPassword}&newPassword=${newPassword}";
    public static final String UPDATE_PASSWORD = "server/user/updatePassword";
    public static final String UPDATE_NAME = "server/user/updateName?name={name}";
    public static final String REGISTER = "server/user/register";
    public static final String GET_TOKEN = "uaa/oauth/token";

    public String getUrl() {
        return url;
    }

}
