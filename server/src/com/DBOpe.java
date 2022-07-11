package com;
import java.sql.*;
import java.util.*;


// ============================数据库的工具库until，供服务器工具库util调用===============================
public class DBOpe {
    
    // 连接数据库url
    // static String url = "jdbc:mysql://mysql.sqlpub.com:3306/chatroom?serverTimezone=UTC";
    // static String user = "hanxdb";
    // static String password = "63ad30bb62e1ac95";
    static String url = "jdbc:mysql://localhost:3306/chatroom?serverTimezone=UTC";
    static String user = "root";
    static String password = "12345";


    // 1.驱动程序加载
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("驱动程序加载成功...");

        } catch (ClassNotFoundException e) {
            System.out.println("驱动程序加载失败...");
        }
    }

    // 创建数据库连接
    public static Connection getConnection() throws SQLException {

        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("数据库连接成功：" + conn);
        return conn;

        
    }
    
    // 查询所有用户信息
    public static List<Map<String, String>> findAll() {

        List<Map<String, String>> list = new ArrayList<>();

        // SQL语句
        String sql = "select user_id, user_pwd, user_name, user_icon from user";

        try (   // 2.创建数据库连接
                Connection conn = DBOpe.getConnection();
                // 3. 创建语句对象
                PreparedStatement pstm = conn.prepareStatement(sql);
                // 5. 执行查询
                ResultSet rs = pstm.executeQuery();) {

            // 6. 遍历结果集
            while (rs.next()) {

                Map<String, String> row = new HashMap<>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));

                list.add(row);
            }

        } catch (SQLException e) {
            System.out.println("数据库查询过程中出现问题...");
        }

        return list;
    }

    // 按照主键查询
    public static Map<String, String> findById(String id) {

        // SQL语句
        String sql = "select user_id,user_pwd,user_name, user_icon from user where user_id = " + id;

        try ( // 2.创建数据库连接
                Connection conn = DBOpe.getConnection();
                // 3. 创建语句对象
                PreparedStatement pstm = conn.prepareStatement(sql);
                // 4. 执行查询（R）
                ResultSet rs = pstm.executeQuery();) {
            
            // 5. 遍历结果集
            if (rs.next()) {

                Map<String, String> row = new HashMap<>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));

                return row;
            }

        } catch (SQLException e) {
            System.out.println("数据库查询过程中出现问题...");
        }

        return null;
    }
    
    // 查询好友 列表
    public static List<Map<String, String>> findFriends(String id) {

        List<Map<String, String>> friends = new ArrayList<>();
        
        // SQL语句
        String sql = "Select user_id,user_pwd,user_name,user_icon FROM user " +
                    " WHERE user_id IN " + 
                                    "(select user_id2 as user_id from friend where user_id1 = " + id + " ) " +
                        " OR user_id IN " +
                                    "(select user_id1 as user_id from friend where user_id2 = " + id + " ) ";
                                    
        try (   // 2.创建数据库连接
                Connection conn = DBOpe.getConnection();
                // 3. 创建语句对象
                PreparedStatement pstm = conn.prepareStatement(sql);
                // 4. 执行查询（R）
                ResultSet rs = pstm.executeQuery();) {

            // 5. 遍历结果集
            while (rs.next()) {

                Map<String, String> row = new HashMap<>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));

                friends.add(row);
            }

        } catch (SQLException e) {
            System.out.println("数据库查询过程中出现问题...");
        }

        return friends;
    }
}
