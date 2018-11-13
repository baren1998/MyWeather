package com.example.android.myweather.Weather;

import java.util.ArrayList;
import java.util.List;

public class Weather {

    private String currentAqi;
    private String currentDegree;
    private String currentCondition;
    private String humidity;
    private String updateTime;
    private String currentWind;
    private String weatherTips;

    // 天气预报列表
    private List<Forecast> mForecastList;

    // 生活指数列表
    private List<LiveIndex> mLiveIndexList;

    public Weather() {
        mForecastList = new ArrayList<>();
        mLiveIndexList = new ArrayList<>();
    }

    public String getCurrentAqi() {
        return currentAqi;
    }

    public String getCurrentDegree() {
        return currentDegree;
    }

    public String getCurrentCondition() {
        return currentCondition;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getCurrentWind() {
        return currentWind;
    }

    public String getWeatherTips() {
        return weatherTips;
    }

    public List<Forecast> getForecastList() {
        return mForecastList;
    }


    public void setCurrentAqi(String currentAqi) {
        this.currentAqi = currentAqi;
    }

    public void setCurrentDegree(String currentDegree) {
        this.currentDegree = currentDegree;
    }

    public void setCurrentCondition(String currentCondition) {
        this.currentCondition = currentCondition;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setCurrentWind(String currentWind) {
        this.currentWind = currentWind;
    }

    public void setWeatherTips(String weatherTips) {
        this.weatherTips = weatherTips;
    }

    public void setForecastList(List<Forecast> forecastList) {
        mForecastList.clear();
        mForecastList.addAll(forecastList);
    }

    public List<LiveIndex> getLiveIndexList() {
        return mLiveIndexList;
    }

    public void setLiveIndexList(List<LiveIndex> liveIndexList) {
        mLiveIndexList.clear();
        mLiveIndexList.addAll(liveIndexList);
    }
}
