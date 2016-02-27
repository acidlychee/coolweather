package com.example.lim.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import java.util.Set;

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
    private Boolean isFromWeatherActivity;
    private static final String SUPPORT_CITY_API = "http://v.juhe.cn/weather/citys?key=1d1bad6d9ea452439b2731aff5a03abf";
    private ImageView iv_back_choosecity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //直接跳到WeatherActivity
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFromWeatherActivity = getIntent().getBooleanExtra("fromCityManager", false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        iv_back_choosecity = (ImageView) findViewById(R.id.iv_back_choosecity);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        Set<String> citySet = sharedPreferences.getStringSet("citySet", null);

        iv_back_choosecity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManagerCitys.class);
                startActivity(intent);
                finish();
            }
        });
        if (citySet != null && !isFromWeatherActivity){
            if (citySet.size()!=0){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }


        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCountry();
                } else if (currentLevel == LEVEL_COUNTRY) {
                    selectedCountry = countryList.get(position);
                    String countryName = selectedCountry.getCountryName();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("countryName", countryName);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();
    }

    private void queryFromServer(final String type) {
        showProgressDialog();
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
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, ManagerCitys.class);
                startActivity(intent);
            }
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
