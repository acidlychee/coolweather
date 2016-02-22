package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.adapter.WeatherAdapter;
import com.example.lim.coolweather.adapter.WeatherVPAdapter;
import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.HoursWeatherBean;
import com.example.lim.coolweather.model.WeatherBean;
import com.example.lim.coolweather.model.WeatherInfo;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;
import com.example.lim.coolweather.view.RefreshableView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {


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
    private String key = "aa32bc7542120890c9f6e8f57a628204";
    private CoolWeatherDB db;

    /**将小圆点的图片用数组表示*/
    private ImageView[] dotsViews;
    //包裹小圆点的LinearLayout
    private ViewGroup dotsGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        db = CoolWeatherDB.getInstance(this);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);
        dotsGroup = (ViewGroup) findViewById(R.id.dots_group);
        //weather_header
        //cityNameText = (TextView)findViewById(R.id.city_name);
        Button changeCity = (Button) findViewById(R.id.change_city);
        //Button refreshWeather = (Button) findViewById(R.id.refesh_weather);
        changeCity.setOnClickListener(this);
        //refreshWeather.setOnClickListener(this);

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
        mViewPager.addOnPageChangeListener(this);
        //dots init
        dotsViews = new ImageView[mViewList.size()];
        for (int i = 0; i < mViewList.size(); i++){

            ImageView dotView = new ImageView(WeatherActivity.this);
            //dotView.setLayoutParams(new ViewGroup.LayoutParams(20,20));//创建一个宽高均为20 的布局
            dotView.setPadding(15, 0, 15, 0);
            dotsViews[i] = dotView;
            if (i == 0){
                dotView.setImageResource(R.drawable.page_indicator_focused);
            }else{
                dotView.setImageResource(R.drawable.page_indicator_unfocused);
            }
            dotsGroup.addView(dotView);
        }
    }

    private void queryWeather(final String country, final RefreshableView rev){
        if (country == null || country.equals("")){
            Log.e("country","country is null");
            return;
        }
        String url = "http://v.juhe.cn/weather/index?format=2&cityname="+country+"&key="+key;
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                try {
                    WeatherBean weatherBean = Utility.parserWeather(new JSONObject(response));
                    if (weatherBean != null) {
                        rev.weatherInfo.setWeatherBean(weatherBean);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather(rev, country);
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void query3HoursWeather(final String country, final RefreshableView rev){
        String url = "http://v.juhe.cn/weather/forecast3h.php?cityname="+country+"&key="+key;
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                try {
                    List<HoursWeatherBean> list = Utility.parserForecast3h(new JSONObject(response));
                    if (list != null) {
                        rev.weatherInfo.setHoursWeatherBeanList(list);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather(rev, country);
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showWeather(RefreshableView rev, String countryName) {
        //List<WeatherInfo> weatherInfoList = db.getWeathers(countryName);
        //WeatherInfo weatherInfo = null;

        rev.mList.clear();
        rev.mList.add(rev.weatherInfo);
        rev.adapter.notifyDataSetChanged();
                //cityNameText.setText(countryName);

                //cityNameText.setVisibility(View.VISIBLE);
/*        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_city:
                Intent intent = new Intent(this, ManagerCitys.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            /*case R.id.refesh_weather:
                Intent intent1 = new Intent(this, ManagerCitys.class);
                startActivity(intent1);
                finish();
                break;
            */default:
                break;
        }
    }

    //为每个RefreshableView的listview对象添加一个单独的adapter
    private void initWeatherView(final View v, Context context, final String country){
        final RefreshableView refreshableView = (RefreshableView) v.findViewById(R.id.refreshable_view);
        refreshableView.adapter = new WeatherAdapter(this, refreshableView.mList);
        refreshableView.cityName = country;
        refreshableView.weatherInfo = new WeatherInfo();
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
                    queryWeather(countryName, refreshableView);
                    query3HoursWeather(countryName, refreshableView);
                    //showWeather(refreshableView, country);
                }
                refreshableView.refreshFinish();
            }
        }, 0);

        queryWeather(country, refreshableView);
        query3HoursWeather(country, refreshableView);
        //showWeather(refreshableView, country);

/*        if (db.getWeathers(country).size()!=0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeather(refreshableView, country);
                }
            });
        }else{
            queryWeather(country, refreshableView);
        }*/

    }


    //viewpager OnPageChangeListener


    @Override
    public void onPageSelected(int position) {
        for (int i=0; i < dotsViews.length; i++ ){
            if (position == i ){
                dotsViews[i].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dotsViews[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
}
