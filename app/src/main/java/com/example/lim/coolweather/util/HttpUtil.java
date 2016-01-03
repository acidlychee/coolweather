package com.example.lim.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lim on 2015/12/29.
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HttpURLConnection connection = null;
                    URL url = new URL(address);
                    connection= (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder strb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        strb.append(line);
                    }
                    if (listener != null){
                        listener.onFinished(strb.toString());
                    }
                }catch (Exception e){
                    if (listener != null){
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }
}
