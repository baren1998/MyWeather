package com.example.android.myweather.Util;

import com.example.android.myweather.db.Province;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HtmlParseUtil {

    /* 爬取并解析墨迹天气的省市数据并存入数据库 */
    public static void ParseProvinceFromWeb() {

        HttpUtil.sendOKHttpRequest("https://tianqi.moji.com/weather/china", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String htmlData = response.body().string();
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

                        Province province = new Province();
                        province.setKey(key);
                        province.setCityQueryUrl(cityQueryUrl);
                        province.setProvinceName(provinceName);
                        province.save();
                    }
                }
            }
        });
    }
}
