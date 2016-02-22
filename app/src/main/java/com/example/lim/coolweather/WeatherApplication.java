package com.example.lim.coolweather; /**
 * Created by lim on 2016/2/21.
 */
import android.app.Application;

import com.thinkland.sdk.android.JuheSDKInitializer;

public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        JuheSDKInitializer.initialize(getApplicationContext());
    }

}
