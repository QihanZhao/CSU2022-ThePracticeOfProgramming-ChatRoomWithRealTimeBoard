package com;

import org.json.JSONObject;

public class Buffer {

    public static DataBox dataToRefreshFriendList = new DataBox();
    public static DataBox dataToRefreshTxtMain = new DataBox();
    public static DataBox dataTxtForALL = new DataBox();
    public static DataBox dataDrawForALL = new DataBox();
    public static DataBox dataFileForALL = new DataBox();

    public static void writeTo(DataBox box, JSONObject data) throws InterruptedException {
        box.write(data);
    }
    
    public static JSONObject readFrom(DataBox box) throws InterruptedException {
        return box.read();
    }
}
