package com.example.android.myweather.db;

public class Province {

    private String provinceName;

    private String cityQueryUrl;

    public Province(String provinceName, String cityQueryUrl) {
        this.provinceName = provinceName;
        this.cityQueryUrl = cityQueryUrl;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityQueryUrl() {
        return cityQueryUrl;
    }

    public void setCityQueryUrl(String cityQueryUrl) {
        this.cityQueryUrl = cityQueryUrl;
    }
}
