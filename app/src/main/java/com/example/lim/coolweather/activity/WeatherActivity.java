package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.adapter.WeatherAdapter;
import com.example.lim.coolweather.service.AutoUpdateService;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;
import com.example.lim.coolweather.view.RefreshableView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {


    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;

    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温
     */
    private TextView temperature;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;

    RefreshableView refreshableView;
    private  List<Map<String,String>> mList;
    private WeatherAdapter mAdaper;
    private SharedPreferences sharePreferece;
    private ListView mlistView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        mList = new ArrayList<>();
/*
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        publishText = (TextView) weatherInfoLayout.findViewById(R.id.publish_text);
        weatherDespText = (TextView) weatherInfoLayout.findViewById(R.id.weather_desp);
        temperature = (TextView) weatherInfoLayout.findViewById(R.id.weather);
        currentDateText = (TextView) weatherInfoLayout.findViewById(R.id.current_date);*/

        //weather_header
        cityNameText = (TextView)findViewById(R.id.city_name);
        Button changeCity = (Button) findViewById(R.id.change_city);
        Button refreshWeather = (Button) findViewById(R.id.refesh_weather);
        changeCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);



        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        mlistView = (ListView) refreshableView.findViewById(R.id.list_view);
        mAdaper = new WeatherAdapter(this,mList);
        mlistView.setAdapter(mAdaper);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                String countryName = sharePreferece.getString("countryName", "");
                //mList.get(0).put("publishtext", "同步中");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(countryName)){
                    queryWeather(countryName);
                }
                refreshableView.refreshFinish();
            }
        }, 0);

        if (getIntent().getStringExtra("countryName") == null){
            showWeather();
        }else{
            queryWeather(getIntent().getStringExtra("countryName"));
        }
    }


    private void queryWeather(final String country){
        if (country.equals("")||country == null){
            return;
        }
        String url = "http://v.juhe.cn/weather/index?format=2&cityname="+country+"&key=1d1bad6d9ea452439b2731aff5a03abf";
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                boolean result = Utility.handleWeatherInfo(WeatherActivity.this, response, country);
                if (result == true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String countryNameStr = sharedPreferences.getString("countryName", "");
        String temperatureStr = sharedPreferences.getString("temperature", "");
        String weatherStr = sharedPreferences.getString("weather", "");
        String dateStr = sharedPreferences.getString("date","");
        String timeStr = sharedPreferences.getString("time", "");

        Map twMap = new HashMap<String, String>();
        twMap.put("temperature", temperatureStr);
        twMap.put("weather", weatherStr);
        twMap.put("date", dateStr);
        /*SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(Calendar.getInstance().getTime());*/
        twMap.put("publishtext",timeStr);

        mList.clear();
        mList.add(twMap);
        mAdaper.notifyDataSetChanged();
        cityNameText.setText(sharedPreferences.getString("countryName",""));
        /*temperature.setText(sharedPreferences.getString("temperature",""));
        weatherDespText.setText(sharedPreferences.getString("weather",""));
        currentDateText.setText(sharedPreferences.getString("date",""));
        publishText.setText(sharedPreferences.getString("time", ""));*/
        
        //futureWeather.setVisibility(View.VISIBLE);
        //weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
/*        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refesh_weather:
                publishText.setText("同步中...");
                String countryName = PreferenceManager.getDefaultSharedPreferences(this).getString("countryName","");
                if (!TextUtils.isEmpty(countryName)){
                    queryWeather(countryName);
                }
                break;
            default:
                break;
        }
    }
}
