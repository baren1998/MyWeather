package com.example.android.myweather.Weather;

public class ForecastSeven {
    private String week;
    private String date;
    private String condition;
    private String conditionImgUrl;
    private String min;
    private String max;

    public ForecastSeven() {
    }

    public ForecastSeven(String week, String date, String condition, String conditionImgUrl, String min, String max) {
        this.week = week;
        this.date = date;
        this.condition = condition;
        this.conditionImgUrl = conditionImgUrl;
        this.min = min;
        this.max = max;
    }

    public String getWeek() {
        return week;
    }

    public String getDate() {
        return date;
    }

    public String getCondition() {
        return condition;
    }

    public String getConditionImgUrl() {
        return conditionImgUrl;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setConditionImgUrl(String conditionImgUrl) {
        this.conditionImgUrl = conditionImgUrl;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMax(String max) {
        this.max = max;
    }
}

