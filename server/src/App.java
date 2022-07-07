import java.sql.*;
import java.util.*;

import com.DBOpe;

public class App {
    public static void main(String[] args) {
        // try {
        //     Class.forName("com.mysql.cj.jdbc.Driver");
        //     System.out.println("驱动程序加载成功...");

        // } catch (ClassNotFoundException e) {
        //     System.out.println("驱动程序加载失败...");
        //     // 退出
        //     return;
        // }

        // String url = "jdbc:mysql://mysql.sqlpub.com:3306/chatroom?serverTimezone=UTC";
        // String user = "hanxdb";
        // String password = "63ad30bb62e1ac95";
        // String url = "jdbc:mysql://localhost:3306/chatroom?serverTimezone=UTC";
        // String user = "root";
        // String password = "12345";

        // try (Connection conn = DriverManager.getConnection(url, user, password)) {

        //     System.out.println("数据库连接成功：" + conn);

        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }


        // SQL语句
        String sql = "select user_id, user_pwd, user_name, user_icon from user";

        try ( // 2.创建数据库连接
                Connection conn = DBOpe.getConnection();
                // 3. 创建语句对象
                PreparedStatement pstm = conn.prepareStatement(sql);
                // 4. 执行查询
                ResultSet rs = pstm.executeQuery();) {


            // 5. 遍历结果集
            while (rs.next()) {

                Map<String, String> row = new HashMap<>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));

                System.out.println(row);
            }

        } catch (SQLException e) {
            System.out.println("数据库查询过程中出现问题...");
            e.printStackTrace();
        }

    }
}
