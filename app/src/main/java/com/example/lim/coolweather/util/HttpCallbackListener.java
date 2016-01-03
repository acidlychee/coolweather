package com.example.lim.coolweather.util;

/**
 * Created by lim on 2015/12/29.
 */
public interface HttpCallbackListener {
    public void onFinished(String response);
    public void onError(Exception e);
}
