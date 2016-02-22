package com.example.lim.coolweather.model;

import java.util.List;

/**
 * Created by lim on 2016/1/4.
 */
public class WeatherInfo {
    WeatherBean weatherBean;
    List<HoursWeatherBean> hoursWeatherBeanList;
    PMBean pmBean;

    public WeatherBean getWeatherBean() {
        return weatherBean;
    }

    public void setWeatherBean(WeatherBean weatherBean) {
        this.weatherBean = weatherBean;
    }

    public List<HoursWeatherBean> getHoursWeatherBeanList() {
        return hoursWeatherBeanList;
    }

    public void setHoursWeatherBeanList(List<HoursWeatherBean> hoursWeatherBeanList) {
        this.hoursWeatherBeanList = hoursWeatherBeanList;
    }

    public PMBean getPmBean() {
        return pmBean;
    }

    public void setPmBean(PMBean pmBean) {
        this.pmBean = pmBean;
    }
}
