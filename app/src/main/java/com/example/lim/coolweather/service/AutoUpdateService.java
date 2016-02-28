package com.example.lim.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.example.lim.coolweather.model.HoursWeatherBean;
import com.example.lim.coolweather.model.WeatherBean;
import com.example.lim.coolweather.model.WeatherInfo;
import com.example.lim.coolweather.receiver.AutoUpdateReceiver;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;
import com.example.lim.coolweather.view.WeatherInfoView;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lim on 2016/1/6.
 */
public class AutoUpdateService extends Service {


    private String key = "aa32bc7542120890c9f6e8f57a628204";
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("weatherservice","create...");
    }

    /**
     * 通过PendingIntent，每隔一段时间启过broadcast，再执行startcommand来后台更新天气
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("weatherservice","stratCommand...");
        if (intent.getBooleanExtra("is_from_broadcast",false)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateAllWeather();
                }
            }).start();
        }
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        long  gapTime = 2*60*60*1000;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+gapTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public  AutoUpdateService getService() {
            return  AutoUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("weatherservice","bindservice...");
        return mBinder;
    }


   ////////////////////////////////////////////////////////////////////////////

    private ParseWeatherInfoCallBack mCallBack;
    private WeatherBean mWeatherBean;
    private List<HoursWeatherBean> mHoursWthList;
    /**
     * ViewPager包含的所有View
     */
    private List<View> mViewList;


    public void setmViewList(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    public void setmCallBack(ParseWeatherInfoCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    /**
     * 完成解析获得weatherInfo后，回调进行天气界面更新
     */
    public interface ParseWeatherInfoCallBack{
        void OnParseWeatherInfoFinish(WeatherInfo weatherInfo, WeatherInfoView wthInfV);
    }

    private void updateAllWeather(){
        Log.i("weather update:", "run....");
        for (View weatherView:
             mViewList) {
            getWeatherInfo(((WeatherInfoView)weatherView).getCityName(),(WeatherInfoView)weatherView);
        }

    }

    public void getWeatherInfo(String country, WeatherInfoView wthInfV){
        CountDownLatch countDownLatch = new CountDownLatch(2);//判断异步任务完成,才能setWeatherBean
        queryWeather(country,countDownLatch);
        query3HoursWeather(country,countDownLatch);
        WeatherInfo weatherInfo = new WeatherInfo();
        mWeatherBean = null;
        mHoursWthList = null;
        try {
            countDownLatch.await();
            weatherInfo.setWeatherBean(mWeatherBean);
            weatherInfo.setHoursWeatherBeanList(mHoursWthList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCallBack.OnParseWeatherInfoFinish(weatherInfo,wthInfV);
    }

    private void queryWeather(final String country,final CountDownLatch countDownLatch){
        if (country == null || country.equals("")){
            Log.e("country", "country is null");
            return;
        }
        String url = "http://v.juhe.cn/weather/index?format=2&cityname="+country+"&key="+key;
        Log.i("queryWeather: ",url);
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                try {
                    mWeatherBean = Utility.parserWeather(new JSONObject(response));
                    countDownLatch.countDown();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                countDownLatch.countDown();
                e.printStackTrace();
            }
        });
    }

    private void query3HoursWeather(final String country, final CountDownLatch countDownLatch){
        String url = "http://v.juhe.cn/weather/forecast3h.php?cityname="+country+"&key="+key;
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                try {
                    mHoursWthList = Utility.parserForecast3h(new JSONObject(response));
                    countDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                countDownLatch.countDown();
                e.printStackTrace();
            }
        });
    }

}
