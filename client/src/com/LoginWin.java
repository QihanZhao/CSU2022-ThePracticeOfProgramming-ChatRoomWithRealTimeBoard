package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;



public class LoginWin extends JFrame {
    
    // 字段：当前屏幕的宽和高
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // 字段：登录窗口宽和高
    private int frameWidth = 329;
    private int frameHeight = 250;

    //构造函数
    public LoginWin() {
        /// 初始化当前窗口
        // =========形式
        setTitle("登录");
        setResizable(false);
        getContentPane().setLayout(null);
        setSize(frameWidth, frameHeight);   // 设置窗口大小
        int x = (int) (screenWidth - frameWidth) / 2;   // 计算窗口位于屏幕中心的坐标
        int y = (int) (screenHeight - frameHeight) / 2;
        setLocation(x, y); // 设置窗口位于屏幕中心
        getContentPane().setBackground(Color.WHITE);

        // =========内容
        // 初始化横幅
        JLabel lbBanner = new JLabel();
        lbBanner.setIcon(new ImageIcon("./src/images/welcome.jpg"));
        lbBanner.setBounds(50, 0, 225, 48);
        // lbBanner.setText("Welcome");
        getContentPane().add(lbBanner);

        // 初始化账户面板
        JPanel pnAccount = new AccountPanel();
        getContentPane().add(pnAccount);

        // 初始化登录按钮
        JButton btLogin = new JButton();
        btLogin.setBounds(130, 181, 63, 19);
        btLogin.setText("登录");
            // 注册登录按钮事件监听器
        btLogin.addActionListener(e -> {
            
            // 先进行用户输入验证，验证通过再登录
            JTextField txtUserId = (JTextField)pnAccount.getComponent(1);
            JPasswordField txtUserPwd = (JPasswordField) pnAccount.getComponent(3);
           
            String userId = txtUserId.getText();
            String password = new String(txtUserPwd.getPassword());

            Map<String, Object> user = Client.login(userId, password);
            if (user != null) {

                System.out.println("登录成功...");
                this.setVisible(false); 

                new Thread(new ClientRunner()).start();   // 启动客户端运行体

                PublicChatWin frame = new PublicChatWin(user);   // 打开公共聊天室界面
                frame.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "账号或密码不正确");
            }

        });
        getContentPane().add(btLogin);

        // =========注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 退出系统
                System.exit(0);
            }
        });
    }
    
    class AccountPanel extends JPanel{
        AccountPanel() {

            // =========形式
            setLayout(null);
            setBounds(7, 54, 300, 118);
            setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2)); // 边框颜色设置为蓝色

            // =========内容
            // 初始化【账号】标签
            JLabel lbUserId = new JLabel();
            lbUserId.setBounds(30, 25, 55, 18);
            lbUserId.setText("账号");
            add(lbUserId);

            // 初始化【账号】文本框
            JTextField txtUserId = new JTextField();
            txtUserId.setBounds(100, 25, 132, 18);
            add(txtUserId);

            // 初始化【密码】标签
            JLabel lbUserPwd = new JLabel();
            lbUserPwd.setBounds(30, 65, 55, 18);
            lbUserPwd.setText("密码");
            add(lbUserPwd);

            // 初始化【密码】密码框
            JPasswordField txtUserPwd = new JPasswordField();
            txtUserPwd.setBounds(100, 65, 132, 18);
            add(txtUserPwd);
        }
    }
    
}
