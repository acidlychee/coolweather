package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.lim.coolweather.model.WeatherInfo;
import com.example.lim.coolweather.service.AutoUpdateService;
import com.example.lim.coolweather.view.WeatherInfoView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {


    private SharedPreferences sharePreferece;
    private ViewPager mViewPager;
    private WeatherVPAdapter vpAdapter;
    /**
     * ViewPager包含的所有View
     */
    private List<View> mViewList;

    /**
     * ViewPager当前的item位置
     */
    private int mCurrentItemIndex;
    /**
     * 将小圆点的图片用数组表示,记录位置
     */
    private List<ImageView> dotsViews;
    /**
     * 包裹小圆点的LinearLayout
     */
    private ViewGroup dotsGroup;
    private AutoUpdateService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);
        dotsGroup = (ViewGroup) findViewById(R.id.dots_group);
        mViewList = new ArrayList<>();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        Button changeCity = (Button) findViewById(R.id.change_city);
        changeCity.setOnClickListener(this);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        bindService(intent,conn,BIND_AUTO_CREATE);

        //加入第一个城市
        String cityName = getIntent().getStringExtra("countryName");
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        if (citySet == null){
            citySet = new TreeSet<>();
        }
        citySet.add(cityName);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove("citySet");
        editor.commit();
        editor.putStringSet("citySet", citySet);
        editor.commit();

    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("weatherservice","connect....success");
            mService = ((AutoUpdateService.LocalBinder)service).getService();
            mService.setmCallBack(new AutoUpdateService.ParseWeatherInfoCallBack() {
                @Override
                public void OnParseWeatherInfoFinish(final WeatherInfo weatherInfo, final WeatherInfoView wthInfV) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wthInfV.mList.clear();
                            wthInfV.mList.add(weatherInfo);
                            wthInfV.adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
            mService.setmViewList(mViewList);
            initViewPager();
            initDots();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    /**
     * 初始化ViewPager及其内容，只在启动应用时调用
     */
    public void initViewPager(){

        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        for (String city:citySet
             ) {
            WeatherInfoView w1 = (WeatherInfoView)LayoutInflater.from(this).inflate(R.layout.pull_reflesh_view, null);
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
        WeatherInfoView w1 = (WeatherInfoView)LayoutInflater.from(this).inflate(R.layout.pull_reflesh_view, null);
        mViewList.add(w1);
        initWeatherView(w1, this, cityName);
        vpAdapter.notifyDataSetChanged();
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
    private void initWeatherView(final WeatherInfoView wthInfV, Context context, final String country){
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
                     //mService.getWeatherInfo(countryName,wthInfV);
                }
                wthInfV.refreshFinish();
            }
        }, 0);
        mService.getWeatherInfo(country,wthInfV);


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
