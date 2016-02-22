package com.example.lim.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.City;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.FutureWeatherBean;
import com.example.lim.coolweather.model.HoursWeatherBean;
import com.example.lim.coolweather.model.Province;
import com.example.lim.coolweather.model.WeatherBean;
import com.example.lim.coolweather.model.WeatherInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    /*public static boolean handleWeatherInfo(Context context, String response, String country){
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
*/
    // 解析城市查询接口
    public static WeatherBean parserWeather(JSONObject json) {

        WeatherBean bean = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        try {
            int code = json.getInt("resultcode");
            int error_code = json.getInt("error_code");
            if (error_code == 0 && code == 200) {
                JSONObject resultJson = json.getJSONObject("result");
                bean = new WeatherBean();

                // toady
                JSONObject todayJson = resultJson.getJSONObject("today");
                bean.setCity(todayJson.getString("city"));
                bean.setUv_index(todayJson.getString("uv_index"));
                bean.setTemp(todayJson.getString("temperature"));
                bean.setWeather_str(todayJson.getString("weather"));
                bean.setWeather_id(todayJson.getJSONObject("weather_id").getString("fa"));
                bean.setDressing_index(todayJson.getString("dressing_index"));

                // sk
                JSONObject skJson = resultJson.getJSONObject("sk");
                bean.setWind(skJson.getString("wind_direction") + skJson.getString("wind_strength"));
                bean.setNow_temp(skJson.getString("temp"));
                bean.setRelease(skJson.getString("time"));
                bean.setHumidity(skJson.getString("humidity"));

                // future

                Date date = new Date(System.currentTimeMillis());
                JSONArray futureArray = resultJson.getJSONArray("future");
                List<FutureWeatherBean> futureList = new ArrayList<FutureWeatherBean>();
                for (int i = 0; i < futureArray.length(); i++) {
                    JSONObject futureJson = futureArray.getJSONObject(i);
                    FutureWeatherBean futureBean = new FutureWeatherBean();
                    Date datef = sdf.parse(futureJson.getString("date"));
                    if (!datef.after(date)) {
                        continue;
                    }
                    futureBean.setTemp(futureJson.getString("temperature"));
                    futureBean.setWeek(futureJson.getString("week"));
                    futureBean.setWeather_id(futureJson.getJSONObject("weather_id").getString("fa"));
                    futureList.add(futureBean);
                    if (futureList.size() == 3) {
                        break;
                    }
                }
                bean.setFutureList(futureList);

            } else {
                //Toast.makeText(getApplicationContext(), "WEATHER_ERROR", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;

    }


    // 解析3小时预报
    public static List<HoursWeatherBean> parserForecast3h(JSONObject json) {
        List<HoursWeatherBean> list = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        Date date = new Date(System.currentTimeMillis());
        try {
            int code = json.getInt("resultcode");
            int error_code = json.getInt("error_code");
            if (error_code == 0 && code == 200) {
                list = new ArrayList<HoursWeatherBean>();
                JSONArray resultArray = json.getJSONArray("result");
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject hourJson = resultArray.getJSONObject(i);
                    Date hDate = sdf.parse(hourJson.getString("sfdate"));
                    if (!hDate.after(date)) {
                        continue;
                    }
                    HoursWeatherBean bean = new HoursWeatherBean();
                    bean.setWeather_id(hourJson.getString("weatherid"));
                    bean.setTemp(hourJson.getString("temp1"));
                    Calendar c = Calendar.getInstance();
                    c.setTime(hDate);
                    bean.setTime(c.get(Calendar.HOUR_OF_DAY) + "");
                    list.add(bean);
                    if (list.size() == 5) {
                        break;
                    }
                }

            } else {
                //Toast.makeText(getApplicationContext(), "HOURS_ERROR", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;

    }
}
