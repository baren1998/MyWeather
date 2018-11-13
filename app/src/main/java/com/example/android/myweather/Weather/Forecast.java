package com.example.android.myweather.Weather;

public class Forecast {
    private String day;
    private String condition;
    private String degree;
    private String wind;
    private String windDirection;
    private String aqi;

    public Forecast(String day, String condition, String degree, String wind, String windDirection, String aqi) {
        this.day = day;
        this.condition = condition;
        this.degree = degree;
        this.wind = wind;
        this.windDirection = windDirection;
        this.aqi = aqi;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getDay() {
        return day;
    }

    public String getCondition() {
        return condition;
    }

    public String getDegree() {
        return degree;
    }

    public String getWind() {
        return wind;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getAqi() {
        return aqi;
    }
}
