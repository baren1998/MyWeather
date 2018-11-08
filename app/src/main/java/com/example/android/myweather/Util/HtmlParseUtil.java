package com.example.android.myweather.Util;

import com.example.android.myweather.db.Province;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.util.List;

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

    public static String extractNameFromHtml(String htmlData) {
        Document document = Jsoup.parse(htmlData);
        Element headElem = document.getElementsByTag("head").first();
        Element titleElem = headElem.getElementsByTag("title").first();
        String title = titleElem.text();
        String name = title.substring(0, title.indexOf("省"));

        return name;
    }
}
