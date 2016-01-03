package com.example.lim.coolweather.util;

import android.text.TextUtils;

import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.City;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

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
}
