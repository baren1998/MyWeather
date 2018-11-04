package com.example.android.myweather;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private List<String> keys = null;

    private ArrayMap<String, ArrayList<Province>> map;

    RecyclerView recyclerView;

    ProvincesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area_frag, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.choose_area_view);

        keys = new ArrayList<>();
        map = new ArrayMap<>();

        adapter = new ProvincesListAdapter(getActivity(), keys, map);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), RecyclerView.VERTICAL));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                    keys.add(key);

                    ArrayList<Province> list = new ArrayList<>();

                    Elements provincesElem = e.getElementsByTag("a");
                    for(Element e1 : provincesElem) {
                        // 获取省份城市查询的url
                        String cityQueryUrl = e1.attr("href");
                        String provinceName = e1.text();

                        Province province = new Province(provinceName, cityQueryUrl);
                        list.add(province);
                    }
                    map.put(key, list);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}
