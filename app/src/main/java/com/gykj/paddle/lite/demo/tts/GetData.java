package com.gykj.paddle.lite.demo.tts;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetData {
    static String  path="http://134.79.10.141:3092/get_vioce_code";
    // 定义一个获取网络图片数据的方法:
    public static String getResult(String text) {
//        URL url = new URL(path);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        // 设置连接超时为5秒
//        conn.setConnectTimeout(5000);
//        // 设置请求类型为Get类型
//        conn.setRequestMethod("POST");
//        // 判断请求Url是否成功
//        if (conn.getResponseCode() != 200) {
//            throw new RuntimeException("请求url失败");
//        }
//        InputStream inStream = conn.getInputStream();
//        byte[] bt = StreamTool.read(inStream);
//        inStream.close();
//        return bt;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("textvalue", text);
            String json = jsonObject.toString();
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();

            android.util.Log.e("tag", "responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream input = conn.getInputStream();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb.append((char) ss);
                }
                android.util.Log.e("tag", "请求结果 = " + sb.toString());
                input.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            android.util.Log.e("tag", "出现异常: " + e.toString());
            e.printStackTrace();
        }
        return sb.toString();

    }

}