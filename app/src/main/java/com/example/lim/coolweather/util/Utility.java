package com.example.lim.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.City;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.Province;
import com.example.lim.coolweather.model.WeatherInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lim on 2015/12/29.
 */
public class Utility {
    public static boolean handelSupportCitysResponse(CoolWeatherDB coolWeatherDB,String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("resultcode").equals("200")){
                JSONArray jarr = jsonObject.getJSONArray("result");
                for (int i = 0; i < jarr.length(); i++) {

                    JSONObject jo = jarr.getJSONObject(i);
                    //保存省份
                    String provinceName = jo.getString("province");
                    int provinceId = coolWeatherDB.getProvinceId(provinceName);
                    if (provinceId == -1){
                        Province province = new Province();
                        province.setProvinceName(provinceName);
                        coolWeatherDB.saveProvince(province);
                    }
                    //保存城市
                    String cityName = jo.getString("city");
                    int cityId = coolWeatherDB.getCityId(cityName);
                    if (cityId == -1){
                        City city = new City();
                        city.setCityName(cityName);
                        city.setProvinceId(coolWeatherDB.getProvinceId(provinceName));
                        coolWeatherDB.saveCity(city);
                    }
                    //保存县
                    String countryName = jo.getString("district");
                    Country country = new Country();
                    country.setCityId(coolWeatherDB.getCityId(cityName));
                    country.setCountryName(countryName);
                    coolWeatherDB.saveCountry(country);
                }
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean handleWeatherInfo(Context context, String response, String country){
        CoolWeatherDB db = CoolWeatherDB.getInstance(context);
        try {
            JSONObject jo = new JSONObject(response);
            if (jo.getString("resultcode").equals("200")){
                jo = jo.getJSONObject("result");
                JSONObject sk = jo.getJSONObject("sk");
                JSONObject today = jo.getJSONObject("today");
                JSONArray future = jo.getJSONArray("future");
                List<WeatherInfo> weatherInfoList = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    WeatherInfo wi = new WeatherInfo();
                    JSONObject jobject = future.getJSONObject(i);
                    wi.setNo(i+1);
                    wi.setWeather(jobject.getString("weather"));
                    wi.setDate(jobject.getString("date"));
                    wi.setTemperature(jobject.getString("temperature"));
                    wi.setCityId(db.getCityId(country));
                    wi.setWeek(jobject.getString("week"));
                    weatherInfoList.add(wi);
                }

                WeatherInfo wi = new WeatherInfo();
                wi.setNo(0);
                wi.setCityId(db.getCityId(country));
                wi.setTemperature(today.getString("temperature"));
                wi.setWeather(today.getString("weather"));
                wi.setTime("同步时间： "+sk.getString("time"));
                wi.setDate(today.getString("date_y"));

                weatherInfoList.add(wi);
                if (db.getWeathers(country).isEmpty()){
                    db.saveWeathers(weatherInfoList);
                }else{
                    db.updateWeathers(weatherInfoList, db.getCityId(country));
                }

                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
