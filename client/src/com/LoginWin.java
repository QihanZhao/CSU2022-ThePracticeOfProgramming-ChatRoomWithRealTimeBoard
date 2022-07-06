package com;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.IOException;
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
        setTitle("QQ登录");
        setResizable(false);
        getContentPane().setLayout(null);
        // 设置窗口大小
        setSize(frameWidth, frameHeight);
        // 计算窗口位于屏幕中心的坐标
        int x = (int) (screenWidth - frameWidth) / 2;
        int y = (int) (screenHeight - frameHeight) / 2;
        // 设置窗口位于屏幕中心
        setLocation(x, y);

        // 初始化横幅
        JLabel lbBanner = new JLabel();
        lbBanner.setBounds(0, 0, 325, 48);
        lbBanner.setText("Welcome");
        getContentPane().add(lbBanner);

        //初始化账户面板
        JPanel pnAccount = getAccountPanel();
        getContentPane().add(pnAccount);

        // 初始化登录按钮
        JButton btLogin = new JButton();
        btLogin.setBounds(130, 181, 63, 19);
        btLogin.setText("登录");
        btLogin.addActionListener(e -> {
            // 注册登录按钮事件监听器
            // 先进行用户输入验证，验证通过再登录

            JTextField txtUserId = (JTextField)pnAccount.getComponent(1);
            JPasswordField txtUserPwd = (JPasswordField) pnAccount.getComponent(3);
            // for test
            // System.out.println(txtUserPwd.getPassword());
            String userId = txtUserId.getText();
            String password = new String(txtUserPwd.getPassword());

            Map user = login(userId, password);
            if (user != null) {

                new Thread(new Client()).start();

                System.out.println("登录成功...");
                this.setVisible(false); // 设置登录窗口不可见
                // 打开公共聊天室界面
                PublicChatWin frame = new PublicChatWin(user);  
                frame.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "账号或密码不正确");
            }

        });
        getContentPane().add(btLogin);

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 退出系统
                System.exit(0);
            }
        });
    
    }
    
    // 客户端向服务器发送登录请求
    Map login(String userId, String password) {
        // 准备一个缓冲区
        byte[] buffer = new byte[1024];
        InetAddress address;
        try {
            address = InetAddress.getByName(Client.SERVER_IP);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", Client.COMMAND_LOGIN);
            jsonObj.put("user_id", userId);
            jsonObj.put("user_pwd", password);
            // 字节数组
            byte[] b = jsonObj.toString().getBytes();
            // 创建DatagramPacket对象
            DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
            // 发送
            Client.socket.send(packet);

            /* 接收数据报 */
            packet = new DatagramPacket(buffer, buffer.length, address, Client.SERVER_PORT);
            Client.socket.receive(packet);
            // 接收数据长度
            int len = packet.getLength();
            String str = new String(buffer, 0, len);
            System.out.println("receivedjsonObj = " + str);
            JSONObject receivedjsonObj = new JSONObject(str);

            if ((Integer) receivedjsonObj.get("result") == Client.LOGIN_SUCCESS) {
                Map user = receivedjsonObj.toMap();
                return user;
            }

        } catch (IOException e) {
            System.out.println("与服务器的IO出错");
            e.printStackTrace();
        }
        return null;
    }

    // 函数：生成账户面板
    JPanel getAccountPanel() {

        JPanel pnAccount = new JPanel();
        pnAccount.setLayout(null);
        pnAccount.setBounds(7, 54, 300, 118);
        // 边框颜色设置为蓝色
        pnAccount.setBorder(BorderFactory.createLineBorder(new Color(102, 153, 255), 1));

        // 初始化【账号】标签
        JLabel lbUserId = new JLabel();
        lbUserId.setBounds(30, 25, 55, 18);
        lbUserId.setText("账号");
        pnAccount.add(lbUserId);

        // 初始化【账号】文本框
        JTextField txtUserId = new JTextField();
        txtUserId.setBounds(100, 25, 132, 18);
        pnAccount.add(txtUserId);

        // 初始化【密码】标签
        JLabel lbUserPwd = new JLabel();
        lbUserPwd.setBounds(30, 65, 55, 18);
        lbUserPwd.setText("密码");
        pnAccount.add(lbUserPwd);

        // 初始化【密码】密码框
        JPasswordField txtUserPwd = new JPasswordField();
        txtUserPwd.setBounds(100, 65, 132, 18);
        pnAccount.add(txtUserPwd);
        
        return pnAccount;
    }
}
