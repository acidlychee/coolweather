package com.example.lim.coolweather.model;

import com.example.lim.coolweather.model.FutureWeatherBean;

import java.util.List;

public class WeatherBean {
	
	private String city;
	private String release;
	private String weather_id;
	private String weather_str;
	private String temp;
	private String now_temp;
	private String felt_temp;
	private String humidity;
	private String wind;
	private String uv_index;
	private String dressing_index;
	
	private List<FutureWeatherBean> futureList;
	private List<HoursWeatherBean> hoursWeatherList;

	public List<HoursWeatherBean> getHoursWeatherList() {
		return hoursWeatherList;
	}

	public void setHoursWeatherList(List<HoursWeatherBean> hoursWeatherList) {
		this.hoursWeatherList = hoursWeatherList;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getWeather_id() {
		return weather_id;
	}

	public void setWeather_id(String weather_id) {
		this.weather_id = weather_id;
	}

	public String getWeather_str() {
		return weather_str;
	}

	public void setWeather_str(String weather_str) {
		this.weather_str = weather_str;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getNow_temp() {
		return now_temp;
	}

	public void setNow_temp(String now_temp) {
		this.now_temp = now_temp;
	}


	public String getFelt_temp() {
		return felt_temp;
	}

	public void setFelt_temp(String felt_temp) {
		this.felt_temp = felt_temp;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}

	public String getUv_index() {
		return uv_index;
	}

	public void setUv_index(String uv_index) {
		this.uv_index = uv_index;
	}

	public String getDressing_index() {
		return dressing_index;
	}

	public void setDressing_index(String dressing_index) {
		this.dressing_index = dressing_index;
	}

	public List<FutureWeatherBean> getFutureList() {
		return futureList;
	}

	public void setFutureList(List<FutureWeatherBean> futureList) {
		this.futureList = futureList;
	}
	
	
	

}
