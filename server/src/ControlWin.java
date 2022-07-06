import org.json.JSONObject;

import javax.print.DocPrintJob;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.DatagramPacket;
import java.text.*;
import java.util.List;
import java.util.Map;
import java.util.*;


public class ControlWin extends JFrame {

    // 获得当前屏幕的宽
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    // 登录窗口宽和高
    private int frameWidth = 260;
    private int frameHeight = 600;

    // RightWindow
    // 用户信息
    private static List<Map<String, String>> users;
    // 用户Label
    private static List<JLabel> lblUsersList;

    // MiddleWindow
    // 查看文本区
    private static JTextArea txtMainInfo;
    // 发送文本区
    private static JTextArea txtInfo;
    // 消息日志( 是查看文本区的冗余信息 )
    private static StringBuffer infoLog;
    // 日期格式
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ControlWin() {

        this.infoLog = new StringBuffer();

        // 服务器信息
        // String userName = "Server/Teacher";
        // TODO    // String userIcon = (String) user.get("user_icon");

        /// 初始化当前Frame
        setBounds((int) screenWidth - 1200, 10, 4 * frameWidth, frameHeight);
        // setLayout(new GridLayout(1,3));
        setLayout(null);
        //TODO // setIconImage(Toolkit.getDefaultToolkit().getImage(PublicChatWin.class.getResource("/images/QQ.png")));

        JPanel panelRight = new InfoPanel();
        panelRight.setBounds(3 * frameWidth, 0, frameWidth, frameHeight);
        getContentPane().add(panelRight);

        JPanel panelMiddle = new TxtPanel();
        panelMiddle.setBounds(2 * frameWidth, 0, frameWidth, frameHeight);
        getContentPane().add(panelMiddle);

        JPanel panelLeft = new SharePanel();
        panelLeft.setBounds(0 * frameWidth, 0, 2 * frameWidth, frameHeight);
        getContentPane().add(panelLeft);

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 退出系统
                System.exit(0);

            }
        });

    }

    class InfoPanel extends JPanel {
        InfoPanel() {
            setLayout(new BorderLayout());

            // Server信息
            JLabel lblLabel = new JLabel("Server/Teacher");
            lblLabel.setHorizontalAlignment(SwingConstants.CENTER);
            // TODO
            add(lblLabel, BorderLayout.NORTH);

            // Users信息
            JPanel panel1 = new UsersPanel();
            add(panel1, BorderLayout.CENTER);
        }

        class UsersPanel extends JPanel {
            UsersPanel() {
                setLayout(new BorderLayout(0, 0));

                JLabel label = new JLabel("所有用户");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                add(label, BorderLayout.NORTH);

                JPanel UsersListPanel = new JPanel();
                UsersListPanel.setLayout(new GridLayout(10, 0, 0, 5));
                add(UsersListPanel);

                // 将所有的好友都挂到面板上，只是通过在线情况设置[状态]——可用/不可用，而非生成实体
                ControlWin.lblUsersList = new ArrayList<JLabel>();
                List<Map<String, String>> users = DB.findAll(); // 取出用户列表
                for (int i = 0; i < users.size(); i++) {
                    // 获得每个好友的信息
                    Map<String, String> user = users.get(i);
                    String userId = user.get("user_id");
                    String userName = user.get("user_name");
                    String userIcon = user.get("user_icon");

                    String state = Server.OFFLINE;
                    for (ClientInfo c : Server.clientList) {
                        if (c.getUserId().equals(userId)) {
                            state = Server.ONLINE;
                            break;
                        }
                    } // 此步多余，服务器上线时，所有用户都不在线

                    // 生成每个好友的label
                    JLabel lblUser = new JLabel(userName);
                    lblUser.setToolTipText(userId);
                    // TODO // lblFriend.setIcon(null);

                    // 根据在线情况，设置可用/不可用
                    if (state.equals(Server.OFFLINE)) {
                        lblUser.setEnabled(false);
                    } else {
                        lblUser.setEnabled(true);
                    }

                    // 设置双击用户的响应：强制下线
                    lblUser.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // 用户图标双击鼠标时显示对话框
                            if (e.getClickCount() == 2) {

                                // 获得用户信息
                                JLabel userlabel = (JLabel) e.getComponent();
                                String userId = userlabel.getToolTipText();

                                // 告知该用户被强制下线
                                for (ClientInfo info : Server.clientList) {

                                    if (info.getUserId().equals(userId)) {

                                        JSONObject jsonObj = new JSONObject();
                                        jsonObj.put("command", Server.COMMAND_QUIT);
                                        byte[] b = jsonObj.toString().getBytes();
                                        DatagramPacket packet = new DatagramPacket(b, b.length, info.getAddress(),
                                                info.getPort());

                                        // 转发给好友
                                        try {
                                            Server.socket.send(packet);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }

                                        break;
                                    }
                                }

                                // 更新服务器端在线用户集合
                                for (ClientInfo info : Server.clientList) {
                                    if (info.getUserId().equals(userId)) {
                                        Server.clientList.remove(info);
                                        break;
                                    }
                                }

                                // 更新服务器端控制面板的在线状态
                                refreshUsersList(userId, Server.OFFLINE);

                                // 向其他在线客户端广播该用户下线
                                try {
                                    Server.broadcastStateChange(userId, Server.OFFLINE);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        }
                    });

                    ControlWin.lblUsersList.add(lblUser); // 添加到列表集合，本质上是一个接口，供外部操作[好友面板]使用
                    UsersListPanel.add(lblUser); // 添加到面板
                }
            }

        }

    }

    class TxtPanel extends JPanel {

        TxtPanel() {
            setLayout(null);

            JPanel panel1 = new PanLine1();
            add(panel1);

            JPanel panel2 = new PanLine2();
            add(panel2);
        }

        class PanLine1 extends JPanel {
            PanLine1() {
                setLayout(null);
                setBounds(new Rectangle(5, 5, 252, 410));
                setBorder(BorderFactory.createLineBorder(Color.blue, 1));

                txtMainInfo = new JTextArea();
                txtMainInfo.setEditable(false);
                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setBounds(5, 5, 242, 400);
                scrollPane.setViewportView(txtMainInfo);

                add(scrollPane);
            }

        }

        class PanLine2 extends JPanel {
            PanLine2() {
                setLayout(null);
                setBounds(5, 420, 252, 138);
                setBorder(BorderFactory.createLineBorder(Color.blue, 1));

                ControlWin.txtInfo = new JTextArea();
                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setBounds(5, 5, 160, 120);
                scrollPane.setViewportView(txtInfo);

                add(scrollPane);
                add(getSendTxtButton());
                add(getSendFileButton());

            }

            // 函数：生成发送按钮
            JButton getSendTxtButton() {

                JButton btSend = new JButton("发消息");
                btSend.setBounds(170, 10, 80, 50);

                btSend.addActionListener(e -> {

                    if (!txtInfo.getText().equals("")) {

                        String date = dateFormat.format(new Date()); // 获得当前时间，并格式化

                        String info = String.format("#%s#" + "\n" + "老师说：%s" + "\n", date, txtInfo.getText());

                        showAndTxtBroadCast(info);
                    }

                    txtInfo.setText("");
                });

                return btSend;
            }

            // 函数：生成发送按钮
            JButton getSendFileButton() {

                JButton btSend = new JButton("发文件");
                btSend.setBounds(170, 65, 80, 50);

                btSend.addActionListener(e -> {

                    JFileChooser myChooser = new ShowFileOpenDialog();

                    int result = myChooser.showOpenDialog(null);
                    File choosedFile = null;
                    if (result == JFileChooser.APPROVE_OPTION) {
                        choosedFile = myChooser.getSelectedFile();
                    }

                    String str = choosedFile.getAbsolutePath();
                    // fileBroadCast(choosedFile);

                    String[] fileDirectory = str.split("\\\\");
                    String fileName = (fileDirectory[fileDirectory.length-1]);
                    // System.out.println(choosedFile);
                    String date = dateFormat.format(new Date()); // 获得当前时间，并格式化
                    String info = String.format("#%s#" + "\n" + "老师发送了文件：%s" + "\n", date, fileName);
                    showAndTxtBroadCast(info);

                    fileBroadCast(choosedFile, fileName);
                    // 
                });

                return btSend;
            }

            class ShowFileOpenDialog extends JFileChooser {

                ShowFileOpenDialog() {
                    setCurrentDirectory(new File("."));

                    // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
                    setFileSelectionMode(JFileChooser.FILES_ONLY);
                    // 设置是否允许多选
                    setMultiSelectionEnabled(true);

                    // 添加可用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名 可变参数）
                    addChoosableFileFilter(new FileNameExtensionFilter("zip(*.zip, *.rar)", "zip", "rar"));
                    // 设置默认使用的文件过滤器
                    setFileFilter(new FileNameExtensionFilter("image(*.jpg, *.png, *.gif)", "jpg", "png", "gif"));

                }

            }
        }

    }
    
    class SharePanel extends JPanel {
        SharePanel() {
            setLayout(new BorderLayout());

            // // Server信息
            // JLabel lblLabel = new JLabel("Server/Teacher");
            // lblLabel.setHorizontalAlignment(SwingConstants.CENTER);
            // // TODO
            // add(lblLabel, BorderLayout.NORTH);

            // // Users信息
            JPanel panel1 = new DrawPanel();
            add(panel1, BorderLayout.CENTER);
        }

        class DrawPanel extends JPanel {
            int flag = 0;
            int lastX = 0;
            int lastY = 0;
            int newX = 0;
            int newY = 0;

            ArrayList<Integer> xPoints;
            ArrayList<Integer> yPoints;

            DrawPanel() {
                xPoints = new ArrayList<Integer>();
                yPoints = new ArrayList<Integer>();

                addMouseListener(new MyMouseListener());
                addMouseMotionListener(new MyMouseMotionListener());
                addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
            }

            private class MyMouseListener extends MouseAdapter {
                @Override
                public void mousePressed(MouseEvent e) {
                    newX = e.getX();
                    newY = e.getY();
                    xPoints.clear();    yPoints.clear();
                    xPoints.add(newX);  yPoints.add(newY);
                    System.out.print(xPoints);
                    System.out.println(yPoints);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    lastX = newX;
                    lastY = newY;
                    newX = e.getX();
                    newY = e.getY();
                    repaint();

                    xPoints.clear();
                    yPoints.clear();

                    JSONObject jObj = new JSONObject();
                    jObj.put("xPoints", xPoints);// body
                    jObj.put("yPoints", yPoints);// body
                    jObj.put("command", Server.COMMAND_DRAWBROADCAST);// head

                    showAndBroadCast(jObj);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    newX = e.getX();
                    newY = e.getY();

                    flag = 2;
                    repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // flag = 3;
                    // repaint();
                }

                // public void mouseExited(MouseEvent e) {
                // }
            }

            private class MyMouseMotionListener extends MouseMotionAdapter {
                @Override
                public void mouseDragged(MouseEvent e) {

                    flag = 1;

                    lastX = newX;
                    lastY = newY;
                    newX = e.getX();
                    newY = e.getY();

                    xPoints.add(newX);
                    yPoints.add(newY);

                    repaint();

                }

                // public void mouseMoved(MouseEvent e) {
                // }
            }

            public void paint(Graphics g) {
                if (flag == 1) {
                    g.setColor(Color.black);
                    g.drawLine(lastX, lastY, newX, newY);
                } else if (flag == 2) {
                    g.setColor(Color.BLUE);
                    g.fillOval(newX, newY, 10, 10);
                } 
                // else if (flag == 3) {

                //     System.out.print(xPoints);
                //     System.out.println(yPoints);

                //     int size = xPoints.size();
                //     g.setColor(Color.GREEN);
                //     int[] x = xPoints.stream().mapToInt(Integer::valueOf).toArray();
                //     int[] y = yPoints.stream().mapToInt(Integer::valueOf).toArray();
                //     g.drawPolyline(x, y, size);
                // }
            }
        }
    }

    
    public static void showAndBroadCast(JSONObject jObj) {

        // 封装报文 并广播 —— 只传输增量信息
        for (ClientInfo cinfo : Server.clientList) {
            // 给其他在线用户广播老师的消息

            byte[] b = jObj.toString().getBytes();
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

    public static void showAndTxtBroadCast(String info) {
        infoLog.append(info);
        txtMainInfo.setText(infoLog.toString());


        // 给其他在线用户广播老师的消息
        JSONObject jObj = new JSONObject();
        jObj.put("message", info);// body
        jObj.put("command", Server.COMMAND_TXTBROADCAST);// head
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

    public static void fileBroadCast(File file, String fileName) {

        long size = file.length(); //以字节byte为单位
        byte[] buffer = new byte[(int)size];
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

    // 刷新用户列表
    public static void refreshUsersList(String userId, String state) {

        System.out.println("刷新用户列表");

        for (JLabel lblUser : lblUsersList) {

            if (userId.equals(lblUser.getToolTipText())) {
                if (state.equals(Server.ONLINE)) {
                    lblUser.setEnabled(true);
                } else {
                    lblUser.setEnabled(false);
                }
            }
        }
    }

}
