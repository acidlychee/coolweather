package com.example.lim.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lim on 2015/12/23.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper {
    private static final String CREATE_PROVINCE = "create table Province ("
                                +"_id integer primary key, "
                                +"province_name text, "
                                +"province_code text)";
    private static final String CREATE_CITY = "create table City ("
                                +"_id integer primary key, "
                                +"city_name text, "
                                +"city_code text, "
                                +"province_id integer)";
    private static final String CREATE_COUNTRY = "create table Country ("
                                +"_id integer primary key, "
                                +"country_name text, "
                                +"country_code text, "
                                +"city_id integer)";
    private static final String CREATE_WEATHERINFO = "create table Weather ("
                                +"_id integer primary key,"
                                +"weather text,"
                                +"date text,"
                                +"temperature text,"
                                +"week text,"
                                +"time text,"
                                +"no integer,"
                                +"city_id integer)";

    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Weather");
        db.execSQL(CREATE_WEATHERINFO);
    }
}
