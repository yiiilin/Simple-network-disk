package cn.linaxhua.file_transfer.client.view;

import cn.linaxhua.file_transfer.client.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;


@Component
public class LoginView extends JFrame {
    @Autowired
    private RegisterView registerView;
    @Autowired
    private MainView mainView;
    @Autowired
    private ApiService apiService;

    private JLabel titleLable = new JLabel("欢迎使用多线程文件传输客户端");
    private JLabel userLabel = new JLabel("账号:");
    private JTextField userText = new JTextField(11);
    private JLabel passwordLabel = new JLabel("密码:");
    private JPasswordField passwordText = new JPasswordField(20);
    private JButton registerButton = new JButton("注册");
    private JButton loginButton = new JButton("登录");

    public LoginView() {
        super();
        this.setTitle("登录");
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

    public void start(Integer uid) {
        this.userText.setText(uid.toString());
        this.setVisible(true);
        this.passwordText.requestFocus();
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
        buttonPanel.add(loginButton);
        container.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setListener() {
        userText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (userText.getText().length() > 50) {
                    userText.setText(userText.getText().substring(0, 50));
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
                if (passwordText.getPassword().length > 20) {
                    passwordText.setText(new String(passwordText.getPassword()).substring(0, 20));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        registerButton.addActionListener(e -> {
            registerView.start();
            this.dispose();
        });
        loginButton.addActionListener(e -> {
            String uid = this.userText.getText();
            String password = String.valueOf(this.passwordText.getPassword());
            Map<String, Object> result;
            try {
                result = apiService.login(uid, password);
            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(this, "登录失败，无法与服务器通信", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (result == null || result.size() == 0) {
                JOptionPane.showMessageDialog(this, "登录失败，无法与服务器通信", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (result.get("error") != null) {
                JOptionPane.showMessageDialog(this, "登录失败，用户名或密码错误", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String name = result.get("name").toString();
            mainView.start(Integer.parseInt(uid), name);
            this.dispose();
        });
    }
}
