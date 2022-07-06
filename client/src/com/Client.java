package com;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.lang.model.util.ElementScanner14;
import javax.swing.JOptionPane;


public class Client implements Runnable {

    // 命令代码
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

    public static String ONLINE = "1";
    public static String OFFLINE = "0";

    // 服务器端IP
    public static String SERVER_IP = "127.0.0.1";
    // 服务器端端口号
    public static int SERVER_PORT = 7788;
    // UDP套接字
    public static DatagramSocket socket;

    // 从服务器接收到的缓存
    public byte[] buffer = new byte[1024];
    public static Data dataToRefreshFriendList;
    public static Data dataToRefreshTxtMain;
    public static Data dataTxtForALL;
    public static Data dataDrawForALL;
    public static Data dataFileForALL;

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
                    // 刷新好友列表
                    // String userId = (String) jsonObj.get("user_id");
                    // String online = (String) jsonObj.get("online");
                    // refreshFriendList(userId, online);
                } else if (cmd == Client.COMMAND_SENDMSG){
                    dataToRefreshTxtMain.write(jsonObj);
                    // 将增量信息显示在[查看区]
                    // String message = (String) jsonObj.get("message"); // 获得增量信息
                    // refreshTxtMain();
                } else if (cmd == Client.COMMAND_QUIT) {
                    // 消息对话框
                    JOptionPane.showMessageDialog(null, "您被强制下线！", "消息提示", JOptionPane.WARNING_MESSAGE); 
                    
                } else if (cmd == Client.COMMAND_TXTBROADCAST) {
                    dataTxtForALL.write(jsonObj);

                } else if (cmd == Client.COMMAND_DRAWBROADCAST) {
                    // System.out.println("受到命令，尝试画图...");
                    dataDrawForALL.write(jsonObj);
                } else if (cmd == COMMAND_FILEBROADCAST) {
                    dataFileForALL.write(jsonObj);
                }

            } catch (Exception e) {
            }
        }
    }
}
