package com.example.android.myweather.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {

    private String key;

    private String provinceName;

    private String cityQueryUrl;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        this.cityQueryUrl = "https://tianqi.moji.com" + cityQueryUrl;
    }
}
