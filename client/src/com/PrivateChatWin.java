package com;

import org.json.JSONObject;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.awt.event.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class PrivateChatWin extends JFrame implements Runnable{

    // 当前用户Id
    private String userId;
    // 聊天好友用户Id
    private String friendUserId;
    // 聊天好友用户名
    private String friendUserName;

    // 查看文本区
    private JTextArea txtMainInfo;
    // 发送文本区
    private JTextArea txtInfo;
    // 消息日志( 是查看文本区的冗余信息 )
    private StringBuffer infoLog;

    // 日期格式
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 获得当前屏幕的高和宽
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    // 登录窗口宽和高
    private int frameWidth = 352;
    private int frameHeight = 310;

    public PrivateChatWin(Map<String, String> user, Map<String, String> friend) {

        this.userId = user.get("user_id");
//TODO  // String userIcon = user.get("user_icon");
        this.friendUserId = friend.get("user_id");
        this.friendUserName = friend.get("user_name");
        this.infoLog = new StringBuffer();


        /// 初始化当前Frame
        setSize(frameWidth, frameHeight);   // 设置Frame大小
        int x = (int) (screenWidth - frameWidth) / 2;   // 计算Frame位于屏幕中心的坐标
        int y = (int) (screenHeight - frameHeight) / 2;
        setLocation(x, y); // 设置Frame位于屏幕中心
        setTitle(String.format("与%s聊天中...", friendUserName));
        setResizable(false);
        getContentPane().setLayout(null);

        // 添加查看面板
        getContentPane().add(getPanLine1());
        // 添加发送面板
        getContentPane().add(getPanLine2());

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        new Thread(this).start();

    }
    
    // 函数：生成查看面板
    private JPanel getPanLine1() {

        JPanel panLine1 = new JPanel();
        panLine1.setLayout(null);
        panLine1.setBounds(new Rectangle(5, 5, 330, 210));
        panLine1.setBorder(BorderFactory.createLineBorder(Color.blue, 1));

        txtMainInfo = new JTextArea();
        txtMainInfo.setEditable(false);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 5, 320, 200);
        scrollPane.setViewportView(txtMainInfo);

        panLine1.add(scrollPane);

        return panLine1;
    }

    // 函数：生成发送面板
    private JPanel getPanLine2() {

        JPanel panLine2 = new JPanel();
        panLine2.setLayout(null);
        panLine2.setBounds(5, 220, 330, 50);
        panLine2.setBorder(BorderFactory.createLineBorder(Color.blue, 1));

        this.txtInfo = new JTextArea();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 5, 222, 40);
        scrollPane.setViewportView(txtInfo);
        
        panLine2.add(scrollPane);
        panLine2.add(getSendButton());

        return panLine2;
    }

    // 函数：生成发送按钮
    private JButton getSendButton() {

        JButton btSend = new JButton("发送");
        btSend.setBounds(232, 10, 90, 30);

        btSend.addActionListener(e -> {

            if (!txtInfo.getText().equals("")) {

                String date = dateFormat.format(new Date()); // 获得当前时间，并格式化

                // 显示在查看区
                String info = String.format("#%s#" + "\n" + "您对%s说：%s" + "\n", date, friendUserName, txtInfo.getText());
                infoLog.append(info);
                txtMainInfo.setText(infoLog.toString());

                // 封装报文 并发送 —— 只传输增量信息
                // body
                Map<String, String> message = new HashMap<String, String>();
                message.put("receive_user_id", friendUserId);
                message.put("user_id", userId);
                message.put("message", txtInfo.getText());
                JSONObject jsonObj = new JSONObject(message);
                // head
                jsonObj.put("command", Client.COMMAND_SENDMSG);
                // send
                try {
                    InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                    byte[] b = jsonObj.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
                    Client.socket.send(packet);

                } catch (IOException ioe) {
                    System.out.println("消息发送失败...");
                }
            }

            txtInfo.setText("");
        });

        return btSend;
    }
    
    // public void refreshTxtMain(String message) {
    //     // 将增量信息显示在[查看区]
    //     String date = dateFormat.format(new Date()); // 获得当前时间，并格式化
    //     String info = String.format("#%s#" + "\n" + "%s对您说：%s" + "\n", date, friendUserName, message); // 将增量信息格式化
    //     infoLog.append(info); // 更新消息日志
    //     txtMainInfo.setText(infoLog.toString()); // 显示在[查看区]
    //     txtMainInfo.setCaretPosition(txtMainInfo.getDocument().getLength()); // 将滚动条拉到底部 // 刷新查看区

    // }
    
    @Override
    public void run() {
        while (true) {
            try {
                JSONObject jsonObj = Client.dataToRefreshTxtMain.read();

                String message = (String) jsonObj.get("message");
                String date = dateFormat.format(new Date()); // 获得当前时间，并格式化
                String info = String.format("#%s#" + "\n" + "%s对您说：%s" + "\n", date, friendUserName, message); // 将增量信息格式化

                infoLog.append(info); // 更新消息日志
                txtMainInfo.setText(infoLog.toString()); // 显示在[查看区]
                txtMainInfo.setCaretPosition(txtMainInfo.getDocument().getLength()); // 将滚动条拉到底部 // 刷新查看区

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    // @Override
    // public void run() {
    //     byte[] buffer = new byte[1024];

    //     while (true) {
    //         try {
    //             System.out.println("it is runinng");
    //             Thread.sleep(1000);
    //             // 接收数据
    //             InetAddress address = InetAddress.getByName(Client.SERVER_IP);
    //             DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Client.SERVER_PORT);
    //             Client.socket.receive(packet);
    //             int len = packet.getLength();
    //             String str = new String(buffer, 0, len);

    //             // 打印接收的数据
    //             System.out.printf("从服务器接收的数据：【%s】\n", str);
    //             JSONObject jsonObj = new JSONObject(str);
                
    //             // 将增量信息显示在[查看区]
    //             String date = dateFormat.format(new Date());    // 获得当前时间，并格式化
    //             String message = (String) jsonObj.get( "message");  //获得增量信息
    //             String info = String.format("#%s#" + "\n" + "%s对您说：%s" + "\n", date, friendUserName, message);  //将增量信息格式化
    //             infoLog.append(info);   //更新消息日志
    //             txtMainInfo.setText(infoLog.toString());    //显示在[查看区]
    //             txtMainInfo.setCaretPosition(txtMainInfo.getDocument().getLength()); //将滚动条拉到底部
                
    //     //原本的代码，将公聊的接收线程停用，私聊接受线程 附加此功能：更新在线好友列表
    //             // Thread.sleep(100);
    //             // // 刷新好友列表
    //             // JSONArray userList = (JSONArray) jsonObj.get("OnlineUserList");

    //             // for (Object item : userList) {

    //             //     JSONObject onlineUser = (JSONObject) item;
    //             //     String userId = (String) onlineUser.get("user_id");
    //             //     String online = (String) onlineUser.get("online");
    //             //     friendsFrame.refreshFriendList(userId, online);
    //             // }

    //         } catch (Exception e) {
    //         }
    //     }
    // }

}
