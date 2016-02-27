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
import android.widget.ListView;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.adapter.WeatherAdapter;
import com.example.lim.coolweather.adapter.WeatherVPAdapter;
import com.example.lim.coolweather.model.HoursWeatherBean;
import com.example.lim.coolweather.model.WeatherBean;
import com.example.lim.coolweather.model.WeatherInfo;
import com.example.lim.coolweather.service.AutoUpdateService;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;
import com.example.lim.coolweather.view.WeatherInfoView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {


    /**
     * 用于显示城市名
     */

    private SharedPreferences sharePreferece;
    private ViewPager mViewPager;
    private WeatherVPAdapter vpAdapter;
    private List<View> mViewList;
    private String key = "aa32bc7542120890c9f6e8f57a628204";
    private int mCurrentItemIndex;
    /**将小圆点的图片用数组表示,记录位置*/
    private List<ImageView> dotsViews;
    //包裹小圆点的LinearLayout
    private ViewGroup dotsGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);
        dotsGroup = (ViewGroup) findViewById(R.id.dots_group);
        mViewList = new ArrayList<>();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        Button changeCity = (Button) findViewById(R.id.change_city);
        changeCity.setOnClickListener(this);

        if (!getIntent().getBooleanExtra("fromMangerCitys",false)){
            initViewPager();
            initDots();
        }
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

    public void initViewPager(){

        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        for (String city:citySet
             ) {
            View w1 = LayoutInflater.from(this).inflate(R.layout.pull_reflesh_view, null);
            mViewList.add(w1);
            initWeatherView(w1, this, city);
        }
        vpAdapter = new WeatherVPAdapter(mViewList, this);
        mViewPager.setAdapter(vpAdapter);
        mViewPager.addOnPageChangeListener(this);
    }

    /**
     * 根据viewPager数据的数量初始化indicator
     */
    public void initDots(){
        dotsViews = new ArrayList<>();
        for (int i = 0; i < mViewList.size(); i++){
            addDot();
        }
        dotsViews.get(0).setImageResource(R.drawable.page_indicator_focused);
    }

    public  void addDot(){
        ImageView dotView = new ImageView(WeatherActivity.this);
        dotView.setPadding(15, 0, 15, 0);
        dotView.setImageResource(R.drawable.page_indicator_unfocused);
        dotsViews.add(dotView);
        dotsGroup.addView(dotView);
    }

    public void addToViewPager(String cityName){
        View w1 = LayoutInflater.from(this).inflate(R.layout.pull_reflesh_view, null);
        mViewList.add(w1);
        initWeatherView(w1, this, cityName);
        vpAdapter.notifyDataSetChanged();
        //dotsGroup.removeViewAt();
    }

    /**
     * 该逻辑只支持单个城市删除
     */
    public void removeViewFromViewPager(){
        mCurrentItemIndex = mViewPager.getCurrentItem();
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        int size = mViewList.size();

        for (int i = 0; i < size; i++){
            WeatherInfoView refreshableView = (WeatherInfoView) mViewList.get(i).findViewById(R.id.refreshable_view);
            if (citySet != null && !citySet.contains(refreshableView.cityName)){
                mViewList.remove(i);
                removeDot(i);
                break;
            }
        }
        vpAdapter.notifyDataSetChanged();
    }

    /**
     * dotsViews 与 dotsGroup的 每一个点的位置应该是相同的
     * 且与mViewList的位置应该也是相同的
     * @param index
     */
    public void removeDot(int index){
        dotsViews.remove(index);
        dotsGroup.removeViewAt(index);
        if (mCurrentItemIndex == index && index <= mViewList.size()-1){
            dotsViews.get(index).setImageResource(R.drawable.page_indicator_focused);
        }

    }

    private void queryWeather(final String country, final WeatherInfoView rev){
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

    private void query3HoursWeather(final String country, final WeatherInfoView rev){
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showWeather(WeatherInfoView rev, String countryName) {

        rev.mList.clear();
        rev.mList.add(rev.weatherInfo);
        rev.adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_city:
                Intent intent = new Intent(this, ManagerCitys.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                //finish();
                break;
            default:
                break;
        }
    }

    //为每个WeatherInfoView的listview对象添加一个单独的adapter
    private void initWeatherView(final View v, Context context, final String country){
        final WeatherInfoView wthInfV = (WeatherInfoView) v.findViewById(R.id.refreshable_view);
        wthInfV.adapter = new WeatherAdapter(this, wthInfV.mList);
        wthInfV.cityName = country;
        wthInfV.weatherInfo = new WeatherInfo();
        ListView mlistView = (ListView) wthInfV.findViewById(R.id.list_view);
        mlistView.setAdapter(wthInfV.adapter);

        wthInfV.setOnRefreshListener(new WeatherInfoView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                String countryName = wthInfV.cityName;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(countryName)) {
                    queryWeather(countryName, wthInfV);
                    //query3HoursWeather(countryName, refreshableView);
                }
                wthInfV.refreshFinish();
            }
        }, 0);

        queryWeather(country, wthInfV);
        //query3HoursWeather(country, refreshableView);

    }



    /**
     * 根据viwpager切换的位置确定当前选择的indicator
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        for (int i=0; i < dotsViews.size(); i++ ){
            if (position == i ){
                dotsViews.get(i).setImageResource(R.drawable.page_indicator_focused);
            }else{
                dotsViews.get(i).setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 当从ManagerCitys返回时，进行处理
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String cityName = intent.getStringExtra("countryName");
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        if (cityName != null ){
/*            if (citySet == null){
                citySet = new TreeSet<>();
            }*/
            citySet.add(cityName);
            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.remove("citySet");
            editor.commit();
            editor.putStringSet("citySet", citySet);
            editor.commit();
            addToViewPager(cityName);
            addDot();
        }
        //有问题，如果同时添加删除，使得size不变。
        if (citySet != null && citySet.size() < mViewList.size()){
            removeViewFromViewPager();
        }
    }

}
