package com.example.android.myweather.Util;

import com.example.android.myweather.R;
import com.example.android.myweather.Weather.Forecast;
import com.example.android.myweather.Weather.LiveIndex;
import com.example.android.myweather.Weather.Weather;
import com.example.android.myweather.db.City;
import com.example.android.myweather.db.Province;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HtmlParseUtil {

    /* 爬取并解析墨迹天气的省市数据并存入数据库 */
    public static void ParseProvincesFromHtml(String htmlData) {
        Document document = Jsoup.parse(htmlData);

        // 提取所有省份类"city_list clearfix"
        Elements cityElements = document.getElementsByClass("city_list clearfix");

        // 提取每个省份的key和省份名称
        for(Element e : cityElements) {
            Element keyElem = e.getElementsByTag("dt").first();
            String key = keyElem.text();

            Elements provincesElem = e.getElementsByTag("a");
            for(Element e1 : provincesElem) {
                // 获取省份城市查询的url
                String cityQueryUrl = e1.attr("href");
                String provinceName = e1.text();

                List<Province> provinceList = LitePal.where("provinceName = ?", provinceName).find(Province.class);
                if(provinceList.size() == 0) {
                    Province province = new Province();
                    province.setKey(key);
                    province.setCityQueryUrl(cityQueryUrl);
                    province.setProvinceName(provinceName);
                    province.save();
                }
            }
        }
    }

    /* */
    public static String extractNameFromHtml(String htmlData) {
        Document document = Jsoup.parse(htmlData);
        Element headElem = document.getElementsByTag("head").first();
        Element titleElem = headElem.getElementsByTag("title").first();
        String title = titleElem.text();
        String name = title.substring(0, title.indexOf("省"));

        return name;
    }

    /* */
    public static void extractCitiesFromHtml(String provinceName ,String htmlData) {
        Document document = Jsoup.parse(htmlData);

        // 根据类"city_hot"提取所有市/区
        Element cityHotElem = document.getElementsByClass("city_hot").first();

        // 根据标签"a"提取每个市/区的天气查询url和市/区名
        Elements elements = cityHotElem.getElementsByTag("a");
        for(Element e : elements) {
            String queryWeatherUrl = e.attr("href");
            String name = e.text();

            List<City> cityList = LitePal.where("provinceName = ? and cityName = ?", provinceName, name)
                    .find(City.class);
            if(cityList.size() == 0) {
                City city = new City();
                city.setProvinceName(provinceName);
                city.setCityName(name);
                city.setQueryWeatherUrl(queryWeatherUrl);
                city.save();
            }
        }
    }

    public static Weather handleWeatherResponse(String htmlData) throws IOException {
        Document document = Jsoup.parse(htmlData);

        Weather weather = new Weather();
        List<Forecast> forecastList = new ArrayList<>();
        List<LiveIndex> liveIndexList = new ArrayList<>();

        /* 先解析并提取当前天气信息 */
        Element wea_infoElem = document.getElementsByClass("wrap clearfix wea_info").first();
        Element wea_alertElem = wea_infoElem.getElementsByClass("wea_alert clearfix").first();
        // 提取当前空气状况
        String currntAqi = wea_alertElem.getElementsByTag("em").first().text();
        weather.setCurrentAqi(currntAqi);

        // 提取当前温度、天气状况和更新时间
        Element wea_weatherElem = wea_infoElem.getElementsByClass("wea_weather clearfix").first();
        String currentDegree = wea_weatherElem.getElementsByTag("em").first().text();
        String currentCondition = wea_weatherElem.getElementsByTag("b").first().text();
        String updateTime = wea_weatherElem.getElementsByClass("info_uptime").first().text();
        weather.setCurrentDegree(currentDegree);
        weather.setCurrentCondition(currentCondition);
        weather.setUpdateTime(updateTime);

        // 提取湿度和风力
        Element wea_aboutElem = wea_infoElem.getElementsByClass("wea_about clearfix").first();
        String humidity = wea_aboutElem.getElementsByTag("span").first().text();
        String currentWind = wea_aboutElem.getElementsByTag("em").first().text();
        weather.setHumidity(humidity);
        weather.setCurrentWind(currentWind);

        // 提取今日天气提示
        Element wea_tipsElem = wea_infoElem.getElementsByClass("wea_tips clearfix").first();
        String weatherTips = wea_tipsElem.getElementsByTag("em").first().text();
        weather.setWeatherTips(weatherTips);

        /* 提取天气预报信息 */
        Element forecastElem = document.getElementsByClass("forecast clearfix").first();
        Elements daysElems = forecastElem.getElementsByClass("days clearfix");
        for(Element e : daysElems) {
            // 提取日子
            String day = e.getElementsByTag("a").first().text();
            // 提取天气状况
            Element imgElem = e.getElementsByTag("img").first();
            String condition = imgElem.attr("alt");
            // 提取当日温度
            Elements liElems = e.getElementsByTag("li");
            Element degreeElem = liElems.get(2);
            String degree = degreeElem.text();
            // 提取风力和风向
            Element windElem = liElems.get(3);
            String windDeriction = windElem.child(0).text();
            String wind = windElem.child(1).text();
            // 提取空气质量
            String aqi = liElems.get(4).getElementsByTag("strong").first().text();

            Forecast forecast = new Forecast(day, condition, degree, wind, windDeriction, aqi);
            forecastList.add(forecast);
        }
        weather.setForecastList(forecastList);

        /* 提取生活指数 */
        Element element = document.getElementsByClass("live_index_grid").first();
        Elements lis = element.getElementsByTag("li");
        for(int i = 0; i < 10; i++) {
            Element e = lis.get(i);
            String title = e.getElementsByTag("dd").first().text();
            String comment = e.getElementsByTag("dt").first().text();
            StringBuilder builder = new StringBuilder("温馨提示：");
            String tips;

            Element a = e.getElementsByTag("a").first();
            String url = a.attr("href");
            if(!url.equals("javascript:")) {
                Request request1 = new Request.Builder().url(url).build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request1).execute();
                String html1 = response.body().string();
                Document document1 = Jsoup.parse(html1);
                Element tipsElem = document1.getElementsByClass("aqi_info_tips").first();
                tips = builder.append(tipsElem.getElementsByTag("dd").first().text()).toString();
            } else {
                tips = builder.append("无").toString();
            }

            LiveIndex liveIndex = new LiveIndex();
            liveIndex.setTitle(title);
            liveIndex.setComment(comment);
            liveIndex.setTips(tips);

            switch (title) {
                case "感冒":
                    liveIndex.setImgResourceId(R.drawable.ic_cold);
                    break;
                case "洗车":
                    liveIndex.setImgResourceId(R.drawable.ic_carwash);
                    break;
                case "空气污染扩散":
                    liveIndex.setImgResourceId(R.drawable.ic_apd);
                    break;
                case "穿衣":
                    liveIndex.setImgResourceId(R.drawable.ic_cloth);
                    break;
                case "旅游":
                    liveIndex.setImgResourceId(R.drawable.ic_tour);
                    break;
                case "交通":
                    liveIndex.setImgResourceId(R.drawable.ic_traffic);
                    break;
                case "化妆":
                    liveIndex.setImgResourceId(R.drawable.ic_makeup);
                    break;
                case "运动":
                    liveIndex.setImgResourceId(R.drawable.ic_sport);
                    break;
                case "钓鱼":
                    liveIndex.setImgResourceId(R.drawable.ic_fishing);
                    break;
                case "紫外线":
                    liveIndex.setImgResourceId(R.drawable.ic_uv);
                    break;
            }
            liveIndexList.add(liveIndex);
        }
        weather.setLiveIndexList(liveIndexList);

        return weather;
    }
}
