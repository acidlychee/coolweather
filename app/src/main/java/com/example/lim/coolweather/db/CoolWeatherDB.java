package com.example.lim.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lim.coolweather.model.City;
import com.example.lim.coolweather.model.Country;
import com.example.lim.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by lim on 2015/12/28.
 */
public class CoolWeatherDB {
    private static final  String DB_NAME = "cool_weather";
    private static final  int VERSION = 1;
    private SQLiteDatabase db;
    private static CoolWeatherDB coolWeatherDB;
    private static final int NOTEXIST = -1;
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbhelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbhelper.getWritableDatabase();
    }

    public synchronized static  CoolWeatherDB getInstance(Context context){
        if (coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    public void saveProvince(Province province){
        if (province != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            contentValues.put("province_code", province.getProvinceCode());
            db.insert("Province",null, contentValues);
        }
    }



    public void saveCity(City city){
        if (city != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("city_code", city.getCityCode());
            contentValues.put("province_id", city.getProvinceId());
            db.insert("City", null, contentValues);
        }
    }



    public void saveCountry(Country country){
        if (country != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("country_name", country.getCountryName());
            contentValues.put("country_code", country.getCountryCode());
            contentValues.put("city_id", country.getCityId());
            db.insert("Country", null, contentValues);
        }
    }

    public List<City> loadCitys(int provinceId){


        List<City> list = new ArrayList<>();


        Cursor cursor = null;
        try {

            cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
            while (cursor.moveToNext()){
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setProvinceId(provinceId);
                list.add(city);
            }
            return list;
        }finally {
            if (cursor!=null){
                cursor.close();
            }
        }
    }

    public List<Country> loadCountrys(int cityId){
        List<Country> list = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = db.query("Country", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
            while (cursor.moveToNext()){
                Country country = new Country();
                country.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
                country.setCityId(cityId);
                list.add(country);
            }
            return list;
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
    }

    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = db.query("Province", null, null, null, null, null, null);
            while (cursor.moveToNext()){
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                list.add(province);
            }
            return list;
        }finally {
            if (cursor!=null){
                cursor.close();
            }
        }
    }

    public int getProvinceId(String provinceName){
        Cursor cursor = null;
        try{

            cursor = db.query("Province", null, "province_name = ?", new String[]{provinceName}, null, null, null);
            if(cursor.moveToFirst()){
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                return id;
            }else{
                return NOTEXIST;
            }
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
    }

    public int getCityId(String cityName){
        Cursor cursor = null;
        try {
            cursor = db.query("City", null, "city_name = ?", new String[]{cityName}, null, null, null);
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                return id;
            } else {
                return NOTEXIST;
            }
        }finally {
            if (cursor!=null){
                cursor.close();
            }
        }
    }
}
