package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.view.LeftSlideDeleteListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ManagerCitys extends Activity {
    List<String> data;
    LeftSlideDeleteListView listView;
    Adapter adapter;
    Toast toast;
    private SharedPreferences sharePreferece;
    ImageView addCity;
    Context mContext;
    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manager_citys);
        mContext = getApplicationContext();
        backBtn = (ImageView) findViewById(R.id.iv_back_citym);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);
        toast= Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        data = new ArrayList<>();
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        addCity = (ImageView) findViewById(R.id.add_city);
        addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(mContext, ChooseAreaActivity.class);
                intent.putExtra("fromCityManager",true);
                startActivity(intent);
                finish();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WeatherActivity.class);
                //使返回weather时，不重新启动weateractivity，而是用原有的weatheracitvity.
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("fromMangerCitys",true);
                startActivity(intent);
                finish();
            }
        });
        for (String city:citySet
                ) {
            data.add(city);
        }

        listView=(LeftSlideDeleteListView)findViewById(R.id.leftslide_citys);

        adapter=new Adapter();
        listView.setAdapter(adapter);

        listView.setOnListViewItemDeleteClickListener(new LeftSlideDeleteListView.OnListViewItemDeleteClickListener() {
            @Override
            public void OnListViewItemDeleteClick(int position) {
                System.out.println(position);
                //toast.setText("删除位置：" + position + "");
                //toast.show();
                data.remove(position);
                SharedPreferences.Editor editor = sharePreferece.edit();
                Set<String> cityset = new TreeSet<String>();
                cityset.addAll(data);
                editor.putStringSet("citySet",cityset);
                editor.commit();
                adapter.notifyDataSetChanged();
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    class Adapter extends BaseAdapter {
        class ViewHolder{
            TextView data;
        }
        ViewHolder holder;
        //不管是否使用缓存布局都支持左划删除
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView==null) {
                holder=new ViewHolder();
                /*
                 * LayoutInflater中第二个参数是布局的父控件，若为null则布局的padding属性不起作用，
                 * 第三个参数若是true，则该方法内部会调用父控件的addView方法把布局加入到其中，并返回父布局。由于ListView不允许使用addView添加控件，所以会抛异常。
                 * 若是false则不调用addView
                 */
                convertView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.citylist_item, listView, false);
                holder.data=(TextView) convertView.findViewById(R.id.data);
                convertView.setTag(holder);
            }
            holder=(ViewHolder) convertView.getTag();
            holder.data.setText(data.get(position)+"");
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
        finish();
    }
}
