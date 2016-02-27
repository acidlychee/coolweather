package com.example.lim.coolweather.view;

import android.content.Context;
import android.util.AttributeSet;

import com.example.lim.coolweather.adapter.WeatherAdapter;
import com.example.lim.coolweather.model.WeatherInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lim on 2016/2/27.
 */
public class WeatherInfoView extends RefreshableView {
    public WeatherAdapter adapter;
    public String cityName;
    public List<WeatherInfo> mList;
    public WeatherInfo weatherInfo;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<WeatherInfo> getmList() {
        return mList;
    }

    public void setmList(List<WeatherInfo> mList) {
        this.mList = mList;
    }

    public WeatherInfo getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public WeatherAdapter getAdapter() {

        return adapter;
    }

    public void setAdapter(WeatherAdapter adapter) {
        this.adapter = adapter;
    }



    public WeatherInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mList = new ArrayList<>();
    }
}
