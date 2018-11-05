package com.example.android.myweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.myweather.Util.HttpUtil;
import com.example.android.myweather.db.Province;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private final String[] totalKeys = {"A", "B", "C", "F", "G", "H", "J", "L", "N", "Q", "S", "T", "X", "Y", "Z"};

    private List<String> currentKeys;
    private ArrayMap<String, List<Province>> map;

    RecyclerView recyclerView;

    ProvincesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area_frag, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.choose_area_view);

        currentKeys = new ArrayList<>();
        map = new ArrayMap<>();
        adapter = new ProvincesListAdapter(getActivity(), currentKeys, map);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), RecyclerView.VERTICAL));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LitePal.getDatabase();
        // 去数据库上查询省份列表，如果没有则墨迹天气网页上爬取数据并解析
        List<Province> provinces = LitePal.findAll(Province.class);
        if(provinces.size() != 0) {
            setMapValue();
            adapter.notifyDataSetChanged();
        } else {
            ParseProvinceFromWeb();
        }
    }

    /* 从数据库查询并设置ArrayMap的值 */
    private void setMapValue() {
        for(int i = 0; i < totalKeys.length; i++) {
            String currentKey = totalKeys[i];
            // 查找当前关键字对应的省份列表
            List<Province> currentProvinces = LitePal.where("key = ?", currentKey)
                    .find(Province.class);
            // 如果查询结果不为空，则将关键字的对应省份列表加入Lisi和ArrayMap
            if(currentProvinces.size() != 0) {
                currentKeys.add(currentKey);
                map.put(currentKey, currentProvinces);
            }
        }
    }

    /* 爬取并解析墨迹天气的省市数据并存入数据库 */
    private void ParseProvinceFromWeb() {

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
                setMapValue();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
