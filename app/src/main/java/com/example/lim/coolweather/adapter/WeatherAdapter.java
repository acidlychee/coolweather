package com.example.lim.coolweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lim.coolweather.R;

import java.util.List;
import java.util.Map;

/**
 * Created by lim on 2016/1/16.
 */
public class WeatherAdapter extends BaseAdapter {

    private final int TYPE_0 = 0;
    private final int TYPE_1 = 1;
    private final Context mContext;
    private List<Map<String,String>> mList;

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

    public WeatherAdapter(Context context, List<Map<String,String>> list) {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.today_weather, null);

                publishText = (TextView) convertView.findViewById(R.id.publish_text);
                weatherInfoLayout = (LinearLayout) convertView.findViewById(R.id.weather_info_layout);

                weatherDespText = (TextView) weatherInfoLayout.findViewById(R.id.weather_desp);
                temperature = (TextView) weatherInfoLayout.findViewById(R.id.weather);
                currentDateText = (TextView) weatherInfoLayout.findViewById(R.id.current_date);
                Map<String,String> map = mList.get(0);
                publishText.setText(map.get("publishtext").toString());
                weatherDespText.setText(map.get("weather").toString());
                temperature.setText(map.get("temperature").toString());
                currentDateText.setText(map.get("date").toString());
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

}
