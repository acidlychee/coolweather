package com.example.lim.coolweather.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.model.FutureWeatherBean;
import com.example.lim.coolweather.model.HoursWeatherBean;
import com.example.lim.coolweather.model.WeatherBean;
import com.example.lim.coolweather.model.WeatherInfo;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by lim on 2016/1/16.
 */
public class WeatherAdapter extends BaseAdapter {

    private final int TYPE_0 = 0;
    private final int TYPE_1 = 1;
    private final Context mContext;
    private List<WeatherInfo> mList;

    class ViewHolder{
        private TextView tv_city,// 城市
                tv_release,// 发布时间
                tv_now_weather,// 天气
                tv_today_temp,// 温度
                tv_now_temp,// 当前温度
                tv_aqi,// 空气质量指数
                tv_quality,// 空气质量
                tv_next_three,// 3小时
                tv_next_six,// 6小时
                tv_next_nine,// 9小时
                tv_next_twelve,// 12小时
                tv_next_fifteen,// 15小时
                tv_next_three_temp,// 3小时温度
                tv_next_six_temp,// 6小时温度
                tv_next_nine_temp,// 9小时温度
                tv_next_twelve_temp,// 12小时温度
                tv_next_fifteen_temp,// 15小时温度
                tv_today_temp_a,// 今天温度a
                tv_today_temp_b,// 今天温度b
                tv_tommorrow,// 明天
                tv_tommorrow_temp_a,// 明天温度a
                tv_tommorrow_temp_b,// 明天温度b
                tv_thirdday,// 第三天
                tv_thirdday_temp_a,// 第三天温度a
                tv_thirdday_temp_b,// 第三天温度b
                tv_fourthday,// 第四天
                tv_fourthday_temp_a,// 第四天温度a
                tv_fourthday_temp_b,// 第四天温度b
                tv_humidity,// 湿度
                tv_wind, tv_uv_index,// 紫外线指数
                tv_dressing_index;// 穿衣指数

        private ImageView iv_now_weather,// 现在
                iv_next_three,// 3小时
                iv_next_six,// 6小时
                iv_next_nine,// 9小时
                iv_next_twelve,// 12小时
                iv_next_fifteen,// 15小时
                iv_today_weather,// 今天
                iv_tommorrow_weather,// 明天
                iv_thirdday_weather,// 第三天
                iv_fourthday_weather;// 第四天
    }
    ViewHolder holder;

    private LinearLayout weatherInfoLayout;
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
    private TextView countryName;

    public WeatherAdapter(Context context, List<WeatherInfo> list) {
        mContext = context;
        mList = list;
    }



    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_0;
        }else{
            return TYPE_1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)){
            case TYPE_0:
                if (convertView == null){
                    holder = new ViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.today_weather_2, null);
                    init(convertView);
                    convertView.setTag(holder);
                }
                holder =(ViewHolder) convertView.getTag();
                setWeatherViews(mList.get(0).getWeatherBean());
                setHourViews(mList.get(0).getHoursWeatherBeanList());
                break;
            case TYPE_1:
                break;
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void init(View convertView){

        holder.tv_now_weather = (TextView) convertView.findViewById(R.id.tv_now_weather);
        holder.tv_today_temp = (TextView) convertView.findViewById(R.id.tv_today_temp);
        holder.tv_now_temp = (TextView) convertView.findViewById(R.id.tv_now_temp);

        holder.tv_aqi = (TextView) convertView.findViewById(R.id.tv_pm_aqi);
        holder.tv_quality = (TextView) convertView.findViewById(R.id.tv_pm_quality);


        holder.iv_now_weather = (ImageView) convertView.findViewById(R.id.iv_now_weather);


        holder.iv_next_three = (ImageView) convertView.findViewById(R.id.iv_next_3);
        holder.iv_next_six = (ImageView) convertView.findViewById(R.id.iv_next_6);
        holder.iv_next_nine = (ImageView) convertView.findViewById(R.id.iv_next_9);
        holder.iv_next_twelve = (ImageView) convertView.findViewById(R.id.iv_next_12);
        holder.iv_next_fifteen = (ImageView) convertView.findViewById(R.id.iv_next_15);

        holder.tv_next_three = (TextView) convertView.findViewById(R.id.tv_next_3);
        holder.tv_next_six = (TextView) convertView.findViewById(R.id.tv_next_6);
        holder.tv_next_nine = (TextView) convertView.findViewById(R.id.tv_next_9);
        holder.tv_next_twelve = (TextView) convertView.findViewById(R.id.tv_next_12);
        holder.tv_next_fifteen = (TextView) convertView.findViewById(R.id.tv_next_15);

        holder.tv_next_three_temp = (TextView) convertView.findViewById(R.id.tv_next_temp_3);
        holder.tv_next_six_temp = (TextView) convertView.findViewById(R.id.tv_next_temp_6);
        holder.tv_next_nine_temp = (TextView) convertView.findViewById(R.id.tv_next_temp_9);
        holder.tv_next_twelve_temp = (TextView) convertView.findViewById(R.id.tv_next_temp_12);
        holder.tv_next_fifteen_temp = (TextView) convertView.findViewById(R.id.tv_next_temp_15);


        holder.iv_today_weather = (ImageView) convertView.findViewById(R.id.iv_1_weather);
        holder.iv_tommorrow_weather = (ImageView) convertView.findViewById(R.id.iv_2_weather);
        holder.iv_thirdday_weather = (ImageView) convertView.findViewById(R.id.iv_3_weather);
        holder.iv_fourthday_weather = (ImageView) convertView.findViewById(R.id.iv_4_weather);

        holder.tv_today_temp_a = (TextView) convertView.findViewById(R.id.tv_1_temp_a);
        holder.tv_today_temp_b = (TextView) convertView.findViewById(R.id.tv_1_temp_b);
        holder.tv_tommorrow = (TextView) convertView.findViewById(R.id.tv_2_weather);
        holder.tv_tommorrow_temp_a = (TextView) convertView.findViewById(R.id.tv_2_temp_a);
        holder.tv_tommorrow_temp_b = (TextView) convertView.findViewById(R.id.tv_2_temp_b);
        holder.tv_thirdday = (TextView) convertView.findViewById(R.id.tv_3_weather);
        holder.tv_thirdday_temp_a = (TextView) convertView.findViewById(R.id.tv_3_temp_a);
        holder.tv_thirdday_temp_b = (TextView) convertView.findViewById(R.id.tv_3_temp_b);
        holder.tv_fourthday = (TextView) convertView.findViewById(R.id.tv_4_weather);
        holder.tv_fourthday_temp_a = (TextView) convertView.findViewById(R.id.tv_4_temp_a);
        holder.tv_fourthday_temp_b = (TextView) convertView.findViewById(R.id.tv_4_temp_a);

        holder.tv_humidity = (TextView) convertView.findViewById(R.id.tv_humidity);
        holder.tv_wind = (TextView) convertView.findViewById(R.id.tv_wind);
        holder.tv_uv_index = (TextView) convertView.findViewById(R.id.tv_uv_index);
        holder.tv_dressing_index = (TextView) convertView.findViewById(R.id.tv_dress_index);
    }

    private void setWeatherViews(WeatherBean bean) {

        //tv_city.setText(bean.getCity());
        //tv_release.setText(bean.getRelease());
        if (bean == null){
            Log.e("error","weatherbean is null");
            return;
        }
        holder.tv_now_weather.setText(bean.getWeather_str());
        String[] tempArr = bean.getTemp().split("~");
        String temp_str_a = tempArr[1].substring(0, tempArr[1].indexOf("℃"));
        String temp_str_b = tempArr[0].substring(0, tempArr[0].indexOf("℃"));
        // 温度 8℃~16℃" ↑ ↓ °
        holder.tv_today_temp.setText("↑ " + temp_str_a + "°   ↓" + temp_str_b + "°");
        holder.tv_now_temp.setText(bean.getNow_temp() + " °");
        holder.iv_today_weather.setImageResource(mContext.getResources().getIdentifier("d" + bean.getWeather_id(), "mipmap", "com.example.lim.coolweather"));

        holder.tv_today_temp_a.setText(temp_str_a + "°");
        holder.tv_today_temp_b.setText(temp_str_b + "°");
        List<FutureWeatherBean> futureList = bean.getFutureList();
        if (futureList != null && futureList.size() == 3) {
            setFutureData(holder.tv_tommorrow, holder.iv_tommorrow_weather, holder.tv_tommorrow_temp_a, holder.tv_tommorrow_temp_b, futureList.get(0));
            setFutureData(holder.tv_thirdday, holder.iv_thirdday_weather, holder.tv_thirdday_temp_a, holder.tv_thirdday_temp_b, futureList.get(1));
            setFutureData(holder.tv_fourthday, holder.iv_fourthday_weather, holder.tv_fourthday_temp_a, holder.tv_fourthday_temp_b, futureList.get(2));
        }
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);
        String prefixStr = null;
        if (time >= 6 && time < 18) {
            prefixStr = "d";
        } else {
            prefixStr = "n";
        }
        holder.iv_now_weather.setImageResource(mContext.getResources().getIdentifier(prefixStr + bean.getWeather_id(), "mipmap", "com.example.lim.coolweather"));

        holder.tv_humidity.setText(bean.getHumidity());
        holder.tv_dressing_index.setText(bean.getDressing_index());
        holder.tv_uv_index.setText(bean.getUv_index());
        holder.tv_wind.setText(bean.getWind());

    }

    private void setHourViews(List<HoursWeatherBean> list) {
        if (list == null){
            Log.e("error","HoursWeatherBeanList is null");
            return;
        }

        setHourData(holder.tv_next_three, holder.iv_next_three, holder.tv_next_three_temp, list.get(0));
        setHourData(holder.tv_next_six, holder.iv_next_six, holder.tv_next_six_temp, list.get(1));
        setHourData(holder.tv_next_nine, holder.iv_next_nine, holder.tv_next_nine_temp, list.get(2));
        setHourData(holder.tv_next_twelve, holder.iv_next_twelve, holder.tv_next_twelve_temp, list.get(3));
        setHourData(holder.tv_next_fifteen, holder.iv_next_fifteen, holder.tv_next_fifteen_temp, list.get(4));
    }

    private void setHourData(TextView tv_hour, ImageView iv_weather, TextView tv_temp, HoursWeatherBean bean) {

        String prefixStr = null;
        int time = Integer.valueOf(bean.getTime());
        if (time >= 6 && time < 18) {
            prefixStr = "d";
        } else {
            prefixStr = "n";
        }

        tv_hour.setText(bean.getTime() + "时");
        iv_weather.setImageResource(mContext.getResources().getIdentifier(prefixStr + bean.getWeather_id(), "mipmap", "com.example.lim.coolweather"));
        tv_temp.setText(bean.getTemp() + "°");
    }

    private void setFutureData(TextView tv_week, ImageView iv_weather, TextView tv_temp_a, TextView tv_temp_b, FutureWeatherBean bean) {
        tv_week.setText(bean.getWeek());
        iv_weather.setImageResource(mContext.getResources().getIdentifier("d" + bean.getWeather_id(), "mipmap", "com.example.lim.coolweather"));
        String[] tempArr = bean.getTemp().split("~");
        String temp_str_a = tempArr[1].substring(0, tempArr[1].indexOf("℃"));
        String temp_str_b = tempArr[0].substring(0, tempArr[0].indexOf("℃"));
        tv_temp_a.setText(temp_str_a + "°");
        tv_temp_b.setText(temp_str_b + "°");

    }
}
