package cn.linaxhua.file_transfer.client.view.panel;

import cn.linaxhua.file_transfer.client.service.ApiService;
import cn.linaxhua.file_transfer.client.view.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@Component
@Scope("prototype")
public class UserInfoPanel extends JPanel {
    @Autowired
    private ApiService apiService;
    @Autowired
    private MainView mainView;
    private JLabel uidLabel = new JLabel("用户id：");
    private JLabel nameLabel = new JLabel("用户名称：");
    private JLabel uidTextLabel = new JLabel();
    private JTextField nameText = new JTextField();

    private JLabel oldPasswordLabel = new JLabel("输入旧密码：");
    private JLabel newPasswordLabel = new JLabel("输入新密码：");
    private JTextField oldPasswrodText = new JTextField();
    private JTextField newPasswrodText = new JTextField();

    private JButton updateNameButton = new JButton("修改昵称");
    private JButton updatePasswordButton = new JButton("修改密码");

    private String name = null;

    public UserInfoPanel() {
        addComponent();
        addListener();
    }

    public void addComponent() {
        uidTextLabel.setPreferredSize(new Dimension(160, 10));
        nameText.setPreferredSize(new Dimension(160, 10));
        oldPasswrodText.setPreferredSize(new Dimension(160, 10));
        newPasswrodText.setPreferredSize(new Dimension(160, 10));

        this.setLayout(new FlowLayout());
        Box baseBox = Box.createVerticalBox();

        Box updateNameBaseBox = Box.createVerticalBox();
        Box updateNameLabelTextBox = Box.createHorizontalBox();

        Box updateNameLabelBox = Box.createVerticalBox();
        updateNameLabelBox.add(uidLabel);
        updateNameLabelBox.add(Box.createVerticalStrut(10));
        updateNameLabelBox.add(nameLabel);

        Box updateNameTextBox = Box.createVerticalBox();
        updateNameTextBox.add(uidTextLabel);
        updateNameTextBox.add(Box.createVerticalStrut(10));
        updateNameTextBox.add(nameText);

        Box updateNameButtonBox = Box.createVerticalBox();
        updateNameButtonBox.add(updateNameButton);
        updateNameLabelTextBox.add(updateNameLabelBox);
        updateNameLabelTextBox.add(Box.createHorizontalStrut(10));
        updateNameLabelTextBox.add(updateNameTextBox);

        updateNameBaseBox.add(updateNameLabelTextBox);
        updateNameBaseBox.add(Box.createVerticalStrut(10));
        updateNameBaseBox.add(updateNameButtonBox);

        Box updatePasswordBaseBox = Box.createVerticalBox();
        Box updatePasswordLabelTextBox = Box.createHorizontalBox();
        Box updatePasswordLabelBox = Box.createVerticalBox();
        updatePasswordLabelBox.add(oldPasswordLabel);
        updatePasswordLabelBox.add(Box.createVerticalStrut(10));
        updatePasswordLabelBox.add(newPasswordLabel);
        Box updatePasswordTextBox = Box.createVerticalBox();
        updatePasswordTextBox.add(oldPasswrodText);
        updatePasswordTextBox.add(Box.createVerticalStrut(10));
        updatePasswordTextBox.add(newPasswrodText);
        Box updatePasswordButtonBox = Box.createVerticalBox();
        updatePasswordButtonBox.add(updatePasswordButton);
        updatePasswordLabelTextBox.add(updatePasswordLabelBox);
        updatePasswordLabelTextBox.add(Box.createHorizontalStrut(10));
        updatePasswordLabelTextBox.add(updatePasswordTextBox);
        updatePasswordBaseBox.add(updatePasswordLabelTextBox);
        updatePasswordBaseBox.add(Box.createVerticalStrut(10));
        updatePasswordBaseBox.add(updatePasswordButtonBox);

        baseBox.add(updateNameBaseBox);
        baseBox.add(Box.createVerticalStrut(20));
        baseBox.add(updatePasswordBaseBox);
        this.add(baseBox);
    }

    public void addListener() {
        nameText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (nameText.getText().length() > 50) {
                    nameText.setText(nameText.getText().substring(0, 50));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        oldPasswrodText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (oldPasswrodText.getText().length() > 20) {
                    oldPasswrodText.setText(oldPasswrodText.getText().substring(0, 20));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        newPasswrodText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (oldPasswrodText.getText().length() > 20) {
                    oldPasswrodText.setText(oldPasswrodText.getText().substring(0, 20));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        updateNameButton.addActionListener(e -> {
            if (name.equals(nameText.getText())) {
                JOptionPane.showMessageDialog(this, "更新昵称失败，两次昵称相同", "更新失败", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (apiService.updateName(nameText.getText())) {
                JOptionPane.showMessageDialog(this, "更新昵称成功", "更新成功", JOptionPane.INFORMATION_MESSAGE);
                this.name = nameText.getText();
                mainView.setUserName(this.name);
            } else {
                JOptionPane.showMessageDialog(this, "更新昵称失败", "更新失败", JOptionPane.WARNING_MESSAGE);
            }
        });
        updatePasswordButton.addActionListener(e -> {
            if (oldPasswrodText.getText().equals(newPasswordLabel.getText())) {
                JOptionPane.showMessageDialog(this, "更新密码失败，两次密码相同", "更新失败", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (apiService.updatePassword(oldPasswrodText.getText(), newPasswrodText.getText())) {
                JOptionPane.showMessageDialog(this, "更新密码成功", "更新成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "更新密码失败", "更新失败", JOptionPane.WARNING_MESSAGE);
            }
        });
    }


    public void showWindow(String uid, String name) {
//        addComponent();
//        addListener();
        this.name = name;
        this.uidTextLabel.setText(uid);
        this.nameText.setText(name);
        this.setVisible(true);
    }

    public void hideWindow() {
        this.setVisible(false);
    }
}
