package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.lim.coolweather.adapter.WeatherVPAdapter;
import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.WeatherInfo;
import com.example.lim.coolweather.service.AutoUpdateService;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;
import com.example.lim.coolweather.view.RefreshableView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {


    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;;

    RefreshableView refreshableView;
    private WeatherAdapter mAdaper;
    private SharedPreferences sharePreferece;
    private ListView mlistView;
    private ViewPager mViewPager;
    private List<View> mViewList;

    private CoolWeatherDB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        db = CoolWeatherDB.getInstance(this);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);

        //weather_header
        cityNameText = (TextView)findViewById(R.id.city_name);
        Button changeCity = (Button) findViewById(R.id.change_city);
        Button refreshWeather = (Button) findViewById(R.id.refesh_weather);
        changeCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        if (getIntent().getStringExtra("countryName")!= null ){
            Set<String> citySet = sharePreferece.getStringSet("citySet", null);
            if (citySet == null){
                citySet = new HashSet<>();
            }
            citySet.add(getIntent().getStringExtra("countryName"));
            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.remove("citySet");
            editor.commit();
            editor.putStringSet("citySet", citySet);
            editor.commit();
        }

        initViewPager();

    }

    public void initViewPager(){
        //init viewPager
        mViewList = new ArrayList<>();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        for (String city:citySet
             ) {
            View w1 = LayoutInflater.from(this).inflate(R.layout.pull_reflesh_view, null);
            mViewList.add(w1);
            initWeatherView(w1, this, city);
        }
        mViewPager.setAdapter(new WeatherVPAdapter(mViewList, this));

    }

    private void queryWeather(final String country, View v){
        final RefreshableView rev = (RefreshableView)v.findViewById(R.id.refreshable_view);
        if (country == null || country.equals("")){
            Log.e("country","country is null");
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
                            showWeather(rev, country);
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

    private void showWeather(RefreshableView rev, String countryName) {
        List<WeatherInfo> weatherInfoList = db.getWeathers(countryName);
        Map twMap = new HashMap<String, String>();
        WeatherInfo weatherInfo = null;
        for (WeatherInfo wi:weatherInfoList
             ) {
            if (wi.getNo() == 0){
                weatherInfo = wi;
                twMap.put("temperature", weatherInfo.getTemperature());
                twMap.put("weather", weatherInfo.getWeather());
                twMap.put("date", weatherInfo.getDate());
                twMap.put("countryName", rev.cityName);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String time = sdf.format(Calendar.getInstance().getTime());
                twMap.put("publishtext", time);
                rev.mList.clear();
                rev.mList.add(twMap);
                rev.adapter.notifyDataSetChanged();
                //cityNameText.setText(countryName);

                //cityNameText.setVisibility(View.VISIBLE);
            }
        }
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
                Intent intent1 = new Intent(this, ManagerCitys.class);
                startActivity(intent1);
                finish();
                break;
            default:
                break;
        }
    }

    //为每个RefreshableView的listview对象添加一个单独的adapter
    private void initWeatherView(final View v, Context context, final String country){
        final RefreshableView refreshableView = (RefreshableView) v.findViewById(R.id.refreshable_view);
        refreshableView.adapter = new WeatherAdapter(this, refreshableView.mList);
        refreshableView.cityName = country;
        ListView mlistView = (ListView) refreshableView.findViewById(R.id.list_view);
        mlistView.setAdapter(refreshableView.adapter);

        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                String countryName = refreshableView.cityName;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(countryName)) {
                    queryWeather(countryName, v);
                }
                refreshableView.refreshFinish();
            }
        }, 0);
        if (db.getWeathers(country).size()!=0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeather(refreshableView, country);
                }
            });
        }else{
            queryWeather(country, v);
        }

    }
}
