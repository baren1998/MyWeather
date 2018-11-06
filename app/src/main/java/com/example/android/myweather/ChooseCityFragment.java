package com.example.android.myweather;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myweather.Util.HttpUtil;

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

public class ChooseCityFragment extends Fragment {

    private String queryCityUrl;
    private RecyclerView cityList;
    private List<String> cities;
    private CityListAdapter adapter;

    private android.support.v7.widget.Toolbar mToolbar;
    private SearchView mSearchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_city_frag, container, false);
        cityList = (RecyclerView) view.findViewById(R.id.city_list);
        cities = new ArrayList<>();

        mSearchView = (SearchView) view.findViewById(R.id.search_view);
        mToolbar = (android.support.v7.widget.Toolbar) view.findViewById(R.id.tool_bar);

        // 设置toolbar
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        adapter = new CityListAdapter(cities);
        // 设置布局管理器
        cityList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
        cityList.setAdapter(adapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        queryCityUrl = ((MainActivity) getActivity()).getQueryCityUrl();
        parseCityFromWeb(queryCityUrl);
    }

    /* 城市列表适配器 */
    class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> implements Filterable {

        private List<String> mSourceList;
        private List<String> mFilterList;

        public CityListAdapter(List<String> list) {
            mSourceList = list;
            mFilterList = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView cityText;

            public ViewHolder(View view) {
                super(view);
                cityText = (TextView) view.findViewById(R.id.city_text);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cities_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final String cityName = mFilterList.get(position);
            holder.cityText.setText(cityName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), cityName, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilterList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                // 执行过滤操作
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    List<String> filteredList;
                    if(TextUtils.isEmpty(charString)) {
                        filteredList = mSourceList;
                    } else {
                        filteredList = new ArrayList<>();
                        for(String str : mSourceList) {
                            if(str.contains(charString)) {
                                filteredList.add(str);
                            }
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredList;
                    return filterResults;
                }

                // 提取过滤后的值
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFilterList = (ArrayList<String>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    private void parseCityFromWeb(String queryUrl) {
        HttpUtil.sendOKHttpRequest(queryUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                Document document = Jsoup.parse(html);

                // 根据类名"city_hot"提取城市列表类
                Element element = document.getElementsByClass("city_hot").first();

                // 根据标签名"a"提取类中每一个标签
                Elements cityNodes = element.getElementsByTag("a");
                for(Element e : cityNodes) {
                    // 提取城市名称
                    String cityName = e.text();
                    cities.add(cityName);
                }
                // 通知数据集更新
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
