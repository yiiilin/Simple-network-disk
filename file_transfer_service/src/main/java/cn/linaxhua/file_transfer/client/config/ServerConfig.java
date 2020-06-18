package cn.linaxhua.file_transfer.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServerConfig {

    @Value("${api-service.server-address}")
    private String serverUrl;
    public static String SERVER_URL;

    @PostConstruct
    public void init() {
        SERVER_URL = serverUrl;
    }

}
