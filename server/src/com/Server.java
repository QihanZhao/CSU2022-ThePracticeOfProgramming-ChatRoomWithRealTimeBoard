package com;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {
    
    // ============================服务器的静态信息，以及运行时维护的动态信息===============================
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

    // 服务器开放端口
    public static int SERVER_PORT = 7788;
    // 服务器端UDP套接字
    public static DatagramSocket socket;
    // 服务器维护一个列表，保存在线客户端的信息
    public static List<ClientInfo> clientList = new CopyOnWriteArrayList<>();
    

    // ============================服务器的运行体，本质是一个解析器parser===============================
    @Override
    public void run() {

        byte[] buffer = new byte[2048];

        while (true) {
            try {
                // 接收数据
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // 转换形式
                int len = packet.getLength(); // 接收数据长度
                String str = new String(buffer, 0, len);
                JSONObject jsonObj = new JSONObject(str);
                System.out.println(jsonObj);
                // 解析数据
                InetAddress address = packet.getAddress(); // 从客户端传来的数据包中得到客户端地址
                int port = packet.getPort(); // 从客户端传来的数据包中得到客户端端口号
                int cmd = (int) jsonObj.get("command");

                if (cmd == COMMAND_LOGIN) {

                    // 获得用户Id，并通过用户Id查询用户信息
                    String userId = (String) jsonObj.get("user_id");
                    Map<String, String> user = DBOpe.findById(userId);

                    // 验证密码
                    boolean pwdIsRight = jsonObj.get("user_pwd").equals(user.get("user_pwd"));

                    // Case1: 登陆成功
                    if (pwdIsRight && user != null) {

                        // 更新服务器端在线用户集合
                        ClientInfo cInfo = new ClientInfo();
                        cInfo.setUserId(userId);
                        cInfo.setAddress(address);
                        cInfo.setPort(port);
                        clientList.add(cInfo);

                        // 更新服务器端控制面板的在线状态
                        ControlWin.refreshUsersList(userId, Server.ONLINE);

                        // 生成返回给用户的数据包
                        JSONObject sendJsonObj = new JSONObject(user);
                        sendJsonObj.put("result", LOGIN_SUCCESS);
                        List<Map<String, String>> friends = getFriendsListWithState(userId);
                        sendJsonObj.put("friends", friends);
                        // 并发送回用户
                        byte[] b = sendJsonObj.toString().getBytes();
                        packet = new DatagramPacket(b, b.length, address, port);
                        socket.send(packet);

                        // 向其他用户广播此用户上线信息
                        broadcastStateChange(userId, ONLINE);

                    } else {
                        // Case2: 登陆失败

                        // 通知用户登陆失败
                        JSONObject sendJsonObj = new JSONObject();
                        sendJsonObj.put("result", LOGIN_FAILURE);
                        byte[] b = sendJsonObj.toString().getBytes();
                        packet = new DatagramPacket(b, b.length, address, port);
                        socket.send(packet);
                    }

                } else if (cmd == COMMAND_SENDMSG) {

                    // 获得好友Id
                    String friendId = (String) jsonObj.get("receive_user_id");

                    // 向客户端发送数据
                    for (ClientInfo info : clientList) {
                        // 找到好友的IP地址和端口号
                        if (info.getUserId().equals(friendId)) {

                            jsonObj.put("command", Server.COMMAND_SENDMSG);
                            byte[] b = jsonObj.toString().getBytes();
                            packet = new DatagramPacket(b, b.length, info.getAddress(), info.getPort());
                            socket.send(packet);

                            break;
                        }
                    }

                } else if (cmd == Server.COMMAND_LOGOUT) {

                    // 获得用户Id
                    String userId = (String) jsonObj.get("user_id");

                    // 更新服务器端在线用户集合
                    for (ClientInfo info : clientList) {
                        if (info.getUserId().equals(userId)) {
                            clientList.remove(info);
                            break;
                        }
                    }

                    // 更新服务器端控制面板的在线状态
                    ControlWin.refreshUsersList(userId, Server.OFFLINE);

                    // 向其他客户端广播该用户下线
                    broadcastStateChange(userId, Server.OFFLINE);

                } else if (cmd == Server.COMMAND_TXTBROADCAST) {

                    // 获得消息内容
                    String info = (String) jsonObj.get("message");

                    ControlWin.showTxtOnServer(info);
                    txtBroadCast(info);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ============================服务器的工具库util===============================
    // ============================供解析器parser调用
    // 1. 获取带有在线状态的好友列表
    private List<Map<String, String>> getFriendsListWithState(String userId) {

        // 取出好友用户列表
        List<Map<String, String>> friends = DBOpe.findFriends(userId);

        // 设置好友状态，添加online字段
        for (Map<String, String> friend : friends) {

            // 添加好友状态 1在线 0离线
            friend.put("online", Server.OFFLINE);

            // 好友在clientList集合中存在，则在线
            String fid = friend.get("user_id");
            for (ClientInfo c : clientList) {
                String uid = c.getUserId();
                if (uid.equals(fid)) {
                    friend.put("online", Server.ONLINE); // 更新好友状态 1在线 0离线
                    break;
                }
            }
        }

        return friends;
    }

    // 2. 广播当前用户上/下线了
    public static void broadcastStateChange(String userId, String state) throws IOException {

        System.out.println("广播当前用户上/下线");

        for (ClientInfo info : clientList) {
            // 给其他在线好友发送，当前用户上/下线消息
            if (!info.getUserId().equals(userId)) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("command", Server.COMMAND_STATE);
                jsonObj.put("user_id", userId);
                jsonObj.put("online", state);

                byte[] b = jsonObj.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(b, b.length, info.getAddress(), info.getPort());
                // 转发给好友
                socket.send(packet);
            }
        }
    }

    // ============================服务器的工具库util===============================
    // ============================供控制面板ControlWin调用
    // 3. 给在线用户广播老师的涂鸦
    public static void drawBroadCast(JSONObject jObj) {
        // 封装报文 并广播 —— 只传输增量信息
        for (ClientInfo cinfo : Server.clientList) {
            

            byte[] b = jObj.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(b, b.length, cinfo.getAddress(), cinfo.getPort());
            try {
                Server.socket.send(packet);
            } catch (IOException e1) {
                System.out.println("消息广播失败...");
                e1.printStackTrace();
            }

        }

    }

    // 4. 给在线用户广播消息
    public static void txtBroadCast(String info) {

        JSONObject jObj = new JSONObject();
        jObj.put("message", info);// body
        jObj.put("command", Server.COMMAND_TXTBROADCAST);// head
        byte[] b = jObj.toString().getBytes();

        // 封装报文 并广播 —— 只传输增量信息
        for (ClientInfo cinfo : Server.clientList) {
            DatagramPacket packet = new DatagramPacket(b, b.length, cinfo.getAddress(), cinfo.getPort());
            try {
                Server.socket.send(packet);
            } catch (IOException e1) {
                System.out.println("消息广播失败...");
                e1.printStackTrace();
            }

        }

    }

    // 5. 给在线用户广播老师的共享文件
    public static void fileBroadCast(File file, String fileName) {

        long size = file.length(); // 以字节byte为单位
        byte[] buffer = new byte[(int) size];
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            in.read(buffer);
        } catch (IOException e) {
            System.out.println("文件读取失败...");
            e.printStackTrace();
        }

        // 给其他在线用户广播老师的消息
        JSONObject jObj = new JSONObject();
        jObj.put("fileName", fileName);// body
        jObj.put("message", buffer);// body
        jObj.put("command", Server.COMMAND_FILEBROADCAST);// head
        byte[] b = jObj.toString().getBytes();

        // 封装报文 并广播 —— 只传输增量信息
        for (ClientInfo cinfo : Server.clientList) {

            DatagramPacket packet = new DatagramPacket(b, b.length, cinfo.getAddress(), cinfo.getPort());
            // 转发给好友
            try {
                Server.socket.send(packet);
            } catch (IOException e1) {
                System.out.println("消息广播失败...");
                e1.printStackTrace();
            }

        }

    }

    // ============================服务器启动器===============================
    public static void main(String[] args) {

        try {
            socket = new DatagramSocket(SERVER_PORT);
            System.out.printf("服务器启动, 监听自己的端口%d...\n", SERVER_PORT);

        } catch (IOException e) {
            System.out.println("服务器启动失败...");
            e.printStackTrace();
        }

        new Thread(new Server()).start();
        new ControlWin().setVisible(true);

    }
    
}
