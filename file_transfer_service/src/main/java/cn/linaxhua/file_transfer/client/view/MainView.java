package cn.linaxhua.file_transfer.client.view;

import cn.linaxhua.file_transfer.client.service.ApiService;
import cn.linaxhua.file_transfer.client.view.panel.JTreePanel;
import cn.linaxhua.file_transfer.client.view.panel.TransferListPanel;
import cn.linaxhua.file_transfer.client.view.panel.UserInfoPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

@Component
public class MainView extends JFrame {
    @Autowired
    private LoginView loginView;
    @Autowired
    private JTreePanel jTreePanel;
    @Autowired
    private TransferListPanel transferListPanel;
    @Autowired
    private UserInfoPanel userInfoPanel;
    private JPanel containerPanel = new JPanel();


    private Integer UID = null;
    private String NAME = null;

    private JButton jTreeButton = new JButton("我的文件");
    private JButton transferListButton = new JButton("传输列表");
    private JButton userInfoButton = new JButton("个人信息");

    public MainView() {
        this.setTitle("多线程文件下载客户端");
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @PostConstruct
    public void init() {
        placeComponents();
        setListener();
    }


    public void start(Integer uid, String name) {
        UID = uid;
        NAME = name;
        jTreePanel.showWindow(uid, name);
        this.setVisible(true);
    }

    private void placeComponents() {
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel functionPanel = new JPanel();
        functionPanel.setLayout(new FlowLayout());
        functionPanel.add(jTreeButton);
        functionPanel.add(transferListButton);
        functionPanel.add(userInfoButton);
        functionPanel.setBorder(new EtchedBorder());
        container.add(functionPanel, BorderLayout.NORTH);

        container.add(containerPanel, BorderLayout.CENTER);
        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(jTreePanel, BorderLayout.CENTER);
    }

    private void setListener() {
        jTreeButton.addActionListener(e -> {
            transferListPanel.hideWindow();
            userInfoPanel.hideWindow();
            jTreePanel.showWindow(UID, NAME);
            containerPanel.removeAll();
            containerPanel.add(jTreePanel, BorderLayout.CENTER);
        });

        transferListButton.addActionListener(e -> {
            jTreePanel.hideWindow();
            userInfoPanel.hideWindow();
            transferListPanel.showWindow(UID);
            containerPanel.removeAll();
            containerPanel.add(transferListPanel, BorderLayout.CENTER);
        });
        userInfoButton.addActionListener(e -> {
            jTreePanel.hideWindow();
            transferListPanel.hideWindow();
            userInfoPanel.showWindow(UID.toString(), NAME);
            containerPanel.removeAll();
            containerPanel.add(userInfoPanel, BorderLayout.CENTER);
        });
    }

    public void setUserName(String name) {
        this.NAME = name;
    }

    public Integer getUID() {
        return UID;
    }
}
