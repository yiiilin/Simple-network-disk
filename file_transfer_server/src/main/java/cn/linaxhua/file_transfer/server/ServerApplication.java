package cn.linaxhua.file_transfer.server;

import cn.linaxhua.file_transfer.server.thread_manager.SocketManager;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.ApplicationContext;

@SpringCloudApplication
public class ServerApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
    }
}
