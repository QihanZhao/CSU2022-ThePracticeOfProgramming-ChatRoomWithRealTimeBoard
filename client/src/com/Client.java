package com;

import org.json.JSONObject;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import javax.swing.JOptionPane;


public class Client implements Runnable {

    // ============================客户端的静态信息，以及运行时维护的动态信息===============================
    // 协议的控制字段
    public static final int COMMAND_LOGIN = 101; // 登录命令
    public static final int COMMAND_LOGOUT = 100; // 注销命令
    public static final int COMMAND_SENDMSG = 333; // 发消息命令
    public static final int COMMAND_TXTBROADCAST = 222; // 广播消息命令
    public static final int COMMAND_DRAWBROADCAST = 666; // 广播图像命令
    public static final int COMMAND_FILEBROADCAST = 777; // 广播文件命令
    public static final int COMMAND_QUIT = 444; // 强制下线命令
    public static final int COMMAND_STATE = 555;
    public static final int LOGIN_SUCCESS = 001;
    public static final int LOGIN_FAILURE = 000;
    public static final String ONLINE = "1";
    public static final String OFFLINE = "0";

    // 服务器端IP
    public static String SERVER_IP = "127.0.0.1";
    // 服务器端端口号
    public static int SERVER_PORT = 7788;
    // UDP套接字
    public static DatagramSocket socket;

    // 从服务器接收到的缓存
    public byte[] buffer = new byte[1024];
    // 根据情况放置到不同的安区域里
    public static Data dataToRefreshFriendList;
    public static Data dataToRefreshTxtMain;
    public static Data dataTxtForALL;
    public static Data dataDrawForALL;
    public static Data dataFileForALL;


    // ============================客户端的运行体，本质是一个解析器parser===============================
    @Override
    public void run() {
        dataToRefreshFriendList = new Data();
        dataToRefreshTxtMain = new Data();
        dataTxtForALL = new Data();
        dataDrawForALL = new Data();
        dataFileForALL = new Data();

        while (true) {
            try {
                // 接收数据
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Client.SERVER_PORT);
                Client.socket.receive(packet);

                // 转换为str
                int len = packet.getLength(); // 接收数据的长度
                String str = new String(buffer, 0, len);
                System.out.printf("从服务器接收的数据： %s\n", str);

                // 转换为json
                JSONObject jsonObj = new JSONObject(str);

                int cmd = (int) jsonObj.get("command");
                if (cmd == Client.COMMAND_STATE) {

                    dataToRefreshFriendList.write(jsonObj);

                } else if (cmd == Client.COMMAND_SENDMSG) {

                    dataToRefreshTxtMain.write(jsonObj);

                } else if (cmd == Client.COMMAND_QUIT) {
                    // 消息对话框
                    JOptionPane.showMessageDialog(null, "您被强制下线！", "消息提示", JOptionPane.WARNING_MESSAGE);

                } else if (cmd == Client.COMMAND_TXTBROADCAST) {

                    dataTxtForALL.write(jsonObj);

                } else if (cmd == Client.COMMAND_DRAWBROADCAST) {

                    dataDrawForALL.write(jsonObj);

                } else if (cmd == COMMAND_FILEBROADCAST) {

                    dataFileForALL.write(jsonObj);
                }

            } catch (Exception e) {
            }
        }
    }
    
    // ============================客户端的工具库util===============================
    // ============================供控制面板ControlWin调用
    // 客户端向服务器发送登录请求
    public static Map<String, Object> login(String userId, String password) {
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
                Map<String, Object> user = receivedjsonObj.toMap();
                return user;
            }

        } catch (IOException e) {
            System.out.println("与服务器的IO出错");
            e.printStackTrace();
        }
        return null;
    }

    // 客户端向服务器发送数据
    public static void sendToServer(byte[] b) {

        try {
            InetAddress address = InetAddress.getByName(Client.SERVER_IP);
            // 创建DatagramPacket对象
            DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
            // 发送
            Client.socket.send(packet);
        } catch (IOException e1) {
        }
    }

    // 客户端保存数据到文件
    public static void saveToFile(byte[] buffer, String fileName) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./src/file/" + fileName))) {
            out.write(buffer);
        } catch (IOException e) {
            System.out.println("文件写入失败...");
            e.printStackTrace();
        }
    }


    // ============================客户端启动器===============================
    // Tip：在身份验证成功之后，才会开启客户端运行体
    public static void main(String[] args) {

        try {
            // 创建DatagramSocket对象，由系统分配可以使用的端口
            socket = new DatagramSocket();
            // 设置超时5秒，不再等待接收数据
            socket.setSoTimeout(5000);
            System.out.println("客户端运行...");

        } catch (IOException e) {
            System.out.println("与服务器连接失败...");
            e.printStackTrace();
        }
        new LoginWin().setVisible(true);
    }

}
