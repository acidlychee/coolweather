package com.example.lim.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.view.LeftSlideDeleteListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManagerCitys extends AppCompatActivity {
    List<String> data;
    LeftSlideDeleteListView listView;
    Adapter adapter;
    Toast toast;
    private SharedPreferences sharePreferece;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharePreferece = PreferenceManager.getDefaultSharedPreferences(this);
        toast= Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        data = new ArrayList<>();
        Set<String> citySet = sharePreferece.getStringSet("citySet", null);
        for (String city:citySet
                ) {
            data.add(city);
        }

        listView=new LeftSlideDeleteListView(getApplicationContext());
        setContentView(listView);
        adapter=new Adapter();
        listView.setAdapter(adapter);

        listView.setOnListViewItemDeleteClickListener(new LeftSlideDeleteListView.OnListViewItemDeleteClickListener() {
            @Override
            public void OnListViewItemDeleteClick(int position) {
                System.out.println(position);
                toast.setText("删除位置：" + position + "");
                toast.show();
                data.remove(position);
                SharedPreferences.Editor editor = sharePreferece.edit();
                Set<String> cityset = new HashSet<String>();
                cityset.addAll(data);
                editor.putStringSet("citySet",cityset);
                editor.commit();
                adapter.notifyDataSetChanged();
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
                convertView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_manager_citys, listView, false);
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
