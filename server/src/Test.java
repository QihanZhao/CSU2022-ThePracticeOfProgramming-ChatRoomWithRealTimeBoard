
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.Server;

public class Test {
    public static void main(String[] args) {
        ArrayList<Integer> xPoints = new ArrayList<Integer>();
        xPoints.add(1);
        xPoints.add(2);
        xPoints.add(3);

        JSONObject jObj = new JSONObject();
        jObj.put("xPoints", xPoints);// body
        jObj.put("command", Server.COMMAND_TXTBROADCAST);// head

        
        JSONArray array =  jObj.getJSONArray("xPoints");
        int size = array.length();
        int[] x = new int[size];
        for (int i = 0; i < size ; i++) {
            x[i] = (array.getInt(i));
            System.out.println(x[i]);
        }
        // System.out.println(jObj);
        
    }
    
}
