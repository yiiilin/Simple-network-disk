package cn.linaxhua.file_transfer.client.view;

import cn.linaxhua.file_transfer.client.service.ApiService;
import cn.linaxhua.file_transfer.client.util.RestTemplateUtil;
import cn.linaxhua.file_transfer.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@Component
public class RegisterView extends JFrame {
    @Autowired
    private LoginView loginView;
    @Autowired
    private ApiService apiService;

    private JLabel titleLable = new JLabel("多线程文件传输系统注册");
    private JLabel userLabel = new JLabel("昵称:");
    private JTextField userText = new JTextField(11);
    private JLabel passwordLabel = new JLabel("密码:");
    private JTextField passwordText = new JPasswordField(20);
    private JButton registerButton = new JButton("注册");
    private JButton cancelButton = new JButton("取消");

    public RegisterView() {
        this.setTitle("注册");
        this.setSize(300, 200);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @PostConstruct
    public void init() {
        placeComponents();
        setListener();
    }

    public void start() {
        this.setVisible(true);
    }

    private void placeComponents() {
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());
        titlePanel.add(titleLable);
        container.add(titlePanel, BorderLayout.NORTH);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(null);
        userLabel.setBounds(50, 20, 50, 20);
        passwordLabel.setBounds(50, 60, 50, 20);
        userText.setBounds(100, 20, 160, 20);
        passwordText.setBounds(100, 60, 160, 20);
        fieldPanel.add(userLabel);
        fieldPanel.add(passwordLabel);
        fieldPanel.add(userText);
        fieldPanel.add(passwordText);
        container.add(fieldPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        container.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setListener() {
        userText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (userText.getText().length() > 11) {
                    userText.setText(userText.getText().substring(0, 11));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        passwordText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (passwordText.getText().length() > 20) {
                    passwordText.setText(passwordText.getText().substring(0, 20));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        registerButton.addActionListener(event -> {
            String userName = userText.getText();
            if (StringUtils.isEmpty(userName)) {
                JOptionPane.showMessageDialog(this, "用户名为空", "错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String password = passwordText.getText();
            if (StringUtils.isEmpty(password)) {
                JOptionPane.showMessageDialog(this, "密码为空", "错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            User user = new User().setName(userName).setPassword(password);
            try {
                Integer uid = apiService.register(user);
                if (uid != -1) {
                    JOptionPane.showMessageDialog(this, "注册成功，您的登录ID为：" + uid, "成功", JOptionPane.INFORMATION_MESSAGE);
                    loginView.start(uid);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "注册失败，无法与服务器通信", "失败", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "注册失败，不可预料的错误", "失败", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelButton.addActionListener(event -> {
            loginView.start();
            this.dispose();
        });
    }
}
