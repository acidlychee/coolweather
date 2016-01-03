package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.db.CoolWeatherDB;
import com.example.lim.coolweather.model.City;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.Province;
import com.example.lim.coolweather.util.HttpCallbackListener;
import com.example.lim.coolweather.util.HttpUtil;
import com.example.lim.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lim on 2015/12/30.
 */
public class ChooseAreaActivity extends Activity {
    private List<String> dataList = new ArrayList<String>();
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<Country> countryList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView titleText;
    private ListView listView;
    private CoolWeatherDB coolWeatherDB;
    private Province selectedProvince;
    private City selectedCity;
    private Country selectedCountry;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private int currentLevel;
    private ProgressDialog progressDialog;

    private static final String SUPPORT_CITY_API = "http://v.juhe.cn/weather/citys?key=1d1bad6d9ea452439b2731aff5a03abf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCity();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCountry();
                }
            }
        });
        queryProvince();
    }

    private void queryFromServer(final String type) {

        HttpUtil.sendHttpRequest(SUPPORT_CITY_API, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                boolean result = false;
                result = Utility.handelSupportCitysResponse(coolWeatherDB, response);
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("Province".equals(type)) {
                                queryProvince();
                            }
                            if ("City".equals(type)) {
                                queryCity();
                            }
                            if ("Country".equals(type)) {
                                queryCountry();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,
                                "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading......");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTRY){
            queryCity();
        }else if (currentLevel == LEVEL_CITY){
            queryProvince();
        }else{
            finish();
        }
    }

    private void queryCountry() {
        countryList = coolWeatherDB.loadCountrys(selectedCity.getId());
        if (!countryList.isEmpty()){

            dataList.clear();
            for (Country country : countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        }
    }

    private void queryProvince(){
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.isEmpty()){
            queryFromServer("Province");
        }else{
            dataList.clear();
            for (Province province: provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }
    }

    private void queryCity() {
        cityList = coolWeatherDB.loadCitys(selectedProvince.getId());
        if (!cityList.isEmpty()){

            dataList.clear();
            for (City city: cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }

    }
}
