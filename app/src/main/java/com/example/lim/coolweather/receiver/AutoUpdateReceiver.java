package com.example.lim.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lim.coolweather.service.AutoUpdateService;

/**
 * Created by lim on 2016/1/6.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        i.putExtra("is_from_broadcast",true);
        context.startService(i);
    }
}
