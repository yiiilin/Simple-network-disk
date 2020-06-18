package cn.linaxhua.file_transfer.client;

import cn.linaxhua.file_transfer.client.view.LoginView;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
public class ServiceApplication {
    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(ServiceApplication.class);

        application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
        ApplicationContext context = application.run(args);

        context.getBean(LoginView.class).start();
    }
}
