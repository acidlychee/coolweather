package com.example.lim.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.lim.coolweather.receiver.AutoUpdateReceiver;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;

/**
 * Created by lim on 2016/1/6.
 */
public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("is_from_broadcast",false)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateWeather();
                }
            }).start();
        }
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        long  gapTime = 5*60*60*1000;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+gapTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        Log.i("weather update:", "run....");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String countryName = sharedPreferences.getString("countryName", "");
        if (!TextUtils.isEmpty(countryName)){
            String url = "http://v.juhe.cn/weather/index?format=2&cityname="+countryName+"&key=1d1bad6d9ea452439b2731aff5a03abf";
            HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
                @Override
                public void onFinished(String response) {
                   //Utility.handleWeatherInfo(AutoUpdateService.this, response, countryName);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
