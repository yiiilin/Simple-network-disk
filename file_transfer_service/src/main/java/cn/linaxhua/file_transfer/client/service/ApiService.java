package cn.linaxhua.file_transfer.client.service;

import cn.linaxhua.file_transfer.client.util.RestTemplateUtil;
import cn.linaxhua.file_transfer.client.view.MainView;
import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.common.entity.User;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiService {
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MainView mainView;
    private static final String CLIENT_ID = "file_transfer";
    private static final String CLIENT_SECRET = "file_transfer";
    private static final String GRANT_TYPE = "password";
    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    private static final String TOKEN_TYPE = "Bearer";
    public static String ACCESS_TOKEN = null;
    public static String REFRESH_TOKEN = null;

    private String getUrl(String apiUrl) {
        return restTemplateUtil.getUrl() + apiUrl;
    }

    public List<Structure> listAllStructure() {
        HttpEntity httpEntity = getTokenHeader();
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.LIST_ALL_STRUCTURE), HttpMethod.GET, httpEntity, String.class);
        JSONObject jsonObject = JSONObject.parseObject(result.getBody());
        if (jsonObject.get("code").toString().equals("200")) {
            List<Structure> structures = JSONObject.parseObject(jsonObject.get("data").toString(), new TypeReference<List<Structure>>() {
            });
            return structures;
        } else {
            return null;
        }
    }

    public Boolean addStructure(Structure structure) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(structure));
        HttpEntity httpEntity = getTokenHeader(null, jsonObject);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.ADD_STRUCTURE), HttpMethod.POST, httpEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            Boolean resultBoolean = JSONObject.parseObject(resultJson.get("data").toString(), Boolean.class);
            return resultBoolean;
        } else {
            return null;
        }
    }

    public Boolean updateStructure(Structure structure) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(structure));
        HttpEntity httpEntity = getTokenHeader(null, jsonObject);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.UPDATE_STRUCTURE), HttpMethod.PUT, httpEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            Boolean resultBoolean = JSONObject.parseObject(resultJson.get("data").toString(), Boolean.class);
            return resultBoolean;
        } else {
            return null;
        }
    }

    public Boolean deleteStructure(Integer id) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id", id);
        HttpEntity httpEntity = getTokenHeader(null, null);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.DELETE_STRUCTURE), HttpMethod.DELETE, httpEntity, String.class, map);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            Boolean resultBoolean = JSONObject.parseObject(resultJson.get("data").toString(), Boolean.class);
            return resultBoolean;
        } else {
            return null;
        }
    }

    public Structure getStructureByPath(String path, String name, Integer uid) {
        Map<String, String> params = new HashMap<>();
        params.put("path", path);
        params.put("name", name);
        params.put("uid", uid.toString());
        HttpEntity httpEntity = getTokenHeader(params, null);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.GET_STRUCTURE), HttpMethod.POST, httpEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            Structure structure = JSONObject.parseObject(resultJson.get("data").toString(), Structure.class);
            return structure;
        } else {
            return null;
        }
    }

    public List<Integer> downloadFile(Structure structure) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(structure));
        HttpEntity httpEntity = getTokenHeader(null, jsonObject);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.DOWNLOAD_FILE), HttpMethod.POST, httpEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            List<Integer> ports = JSONObject.parseObject(resultJson.get("data").toString(), new TypeReference<List<Integer>>() {
            });
            return ports;
        } else {
            return null;
        }
    }

    public List<Integer> uploadFile(Structure structure) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(structure));
        HttpEntity httpEntity = getTokenHeader(null, jsonObject);
        ResponseEntity<String> result = restTemplate.exchange(getUrl(RestTemplateUtil.UPLOAD_FILE), HttpMethod.POST, httpEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result.getBody());
        if (resultJson.get("code").toString().equals("200")) {
            if (resultJson.get("data") == null) {
                return null;
            }
            List<Integer> ports = JSONObject.parseObject(resultJson.get("data").toString(), new TypeReference<List<Integer>>() {
            });
            return ports;
        } else {
            return null;
        }
    }

    public Boolean updatePassword(String oldPassword, String newPassword) {
        Map<String, String> map = new HashMap<>();
        map.put("oldPassword", oldPassword);
        map.put("newPassword", newPassword);
        HttpEntity httpEntity = getTokenHeader(map, null);
        String result = null;
        try {
            result = restTemplate.patchForObject(getUrl(RestTemplateUtil.UPDATE_PASSWORD), httpEntity, String.class);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        //格式化结果
        Map<String, Object> resultMap = JSONObject.parseObject(result, Map.class);
        if (resultFilter(resultMap)) {
            return updatePassword(oldPassword, newPassword);
        }
        if ((Boolean) resultMap.get("data")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Boolean updateName(String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        HttpEntity httpEntity = getTokenHeader();
        String result = null;
        try {
            result = restTemplate.patchForObject(getUrl(RestTemplateUtil.UPDATE_NAME), httpEntity, String.class, map);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        //格式化结果
        Map<String, Object> resultMap = JSONObject.parseObject(result, Map.class);
        if (resultFilter(resultMap)) {
            return updateName(name);
        }
        if ((Boolean) resultMap.get("data")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Integer register(User user) {
        HttpEntity requestEntity = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"));
        String json = JSONObject.toJSONString(user);
        requestEntity = new HttpEntity(json, headers);
        String resultJson = restTemplate.postForObject(getUrl(RestTemplateUtil.REGISTER), requestEntity, String.class);
        Map<String, Object> result = JSONObject.parseObject(resultJson, HashMap.class);
        if (result.get("code").toString().equals("200")) {
            return Integer.parseInt(result.get("data").toString());
        } else {
            return -1;
        }
    }

    public Map<String, Object> login(String uid, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", CLIENT_ID);
        map.add("client_secret", CLIENT_SECRET);
        map.add("grant_type", GRANT_TYPE);
        map.add("username", uid);
        map.add("password", password);
        String result;
        try {
            result = restTemplate.postForObject(getUrl(RestTemplateUtil.GET_TOKEN), map, String.class);
        } catch (HttpClientErrorException e) {
            result = e.getMessage();
            Map<String, Object> resultMap = new HashMap<>();
            if (result.split(":")[0].trim().equals("400")) {
                resultMap.put("error", "用户名或密码错误");
            }
            return resultMap;
        }

        //格式化结果
        Map<String, Object> resultMap = JSONObject.parseObject(result, Map.class);
        if (resultMap.get("uid").toString().equals(uid)) {
            ACCESS_TOKEN = resultMap.get("access_token").toString();
            REFRESH_TOKEN = resultMap.get("refresh_token").toString();
            return resultMap;
        } else {
            return null;
        }
    }

    public void refreshToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", CLIENT_ID);
        map.add("client_secret", CLIENT_SECRET);
        map.add("grant_type", REFRESH_TOKEN_GRANT_TYPE);
        map.add("refresh_token", REFRESH_TOKEN);
        String result = null;
        try {
            result = restTemplate.postForObject(getUrl(RestTemplateUtil.GET_TOKEN), map, String.class);
        } catch (HttpClientErrorException e) {
            JOptionPane.showMessageDialog(mainView, "令牌失效，请重启程序重新登录", "令牌失效", JOptionPane.WARNING_MESSAGE);
        }
        //格式化结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        Map<String, Object> resultMap = JSONObject.parseObject(result, Map.class);
        if (jsonObject.get("uid") != null) {
            ACCESS_TOKEN = resultMap.get("access_token").toString();
            REFRESH_TOKEN = resultMap.get("refresh_token").toString();
        } else {
            JOptionPane.showMessageDialog(mainView, "令牌失效，请重启程序重新登录", "令牌失效", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 刷新令牌，在2小时的令牌失效后自动刷新令牌
     *
     * @param map 结果
     * @return 是否需要重新启动方法
     */
    private Boolean resultFilter(Map<String, Object> map) {
        if (map.get("code").toString().equals("401")) {
            refreshToken();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 自动装填token
     *
     * @param map 额外装填内容
     * @return 返回http体
     */
    private HttpEntity getTokenHeader(Map<String, String> map, JSONObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"));
        headers.add("Authorization", TOKEN_TYPE + " " + ACCESS_TOKEN);
        if (map == null) {
            map = new HashMap<>();
        }
        if (jsonObject != null) {
            for (Map.Entry entry : jsonObject.entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        String json = JSONObject.toJSONString(map);
        return new HttpEntity(json, headers);
    }

    private HttpEntity getTokenHeader() {
        return getTokenHeader(null, null);
    }

}
