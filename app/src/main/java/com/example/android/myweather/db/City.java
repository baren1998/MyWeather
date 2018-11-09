package com.example.android.myweather.db;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {

    private String provinceName;

    private String cityName;

    private String queryWeatherUrl;

    public String getProvinceName() {
        return provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getQueryWeatherUrl() {
        return queryWeatherUrl;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setQueryWeatherUrl(String queryWeatherUrl) {
        this.queryWeatherUrl = queryWeatherUrl;
    }
}
