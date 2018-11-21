package com.example.android.myweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import com.example.android.myweather.Util.HtmlParseUtils;
import com.example.android.myweather.Util.HttpUtils;
import com.example.android.myweather.db.City;
import com.example.android.myweather.db.Province;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseCityFragment extends Fragment {

    private Province currentProvince;
    private RecyclerView cityList;
    private List<City> cities;
    private CityListAdapter adapter;

    private android.support.v7.widget.Toolbar mToolbar;
    private SearchView mSearchView;
    private FloatingActionButton fab;
    private AlertDialog dialog;

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

        // 初始化fab
        fab = (FloatingActionButton) view.findViewById(R.id.fab_refresh);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 获取MainActivity中的Province对象
        currentProvince = ((MainActivity) getActivity()).getCurrentProvince();

        // 先去数据库查询城市列表，若数据库无数据则去Web上爬取
        List<City> list = LitePal.where("provinceName = ?", currentProvince.getProvinceName()).find(City.class);
        if(list.size() != 0) {
            setListValue();
            adapter.notifyDataSetChanged();
        } else {
            parseCityFromWeb(currentProvince);
        }

        // 为SearchView设置监听器
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

        // 设置fab点击事件
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击重新拉取所有城市
                parseCityFromWeb(currentProvince);
            }
        });
    }

    /* 城市列表适配器 */
    class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> implements Filterable {

        private List<City> mSourceList;
        private List<City> mFilterList;

        public CityListAdapter(List<City> list) {
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
            final City currentCity = mFilterList.get(position);
            final String cityName = currentCity.getCityName();
            holder.cityText.setText(cityName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 使用EventBus发送黏性事件，跳转到WeatherActivity并进行处理
                    EventBus.getDefault().postSticky(currentCity);
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
//                    String weatherQueryUrl = currentCity.getQueryWeatherUrl();
//                    intent.putExtra("city", cityName);
//                    intent.putExtra("weatherQuery", weatherQueryUrl);
                    startActivity(intent);
                }
            });

            // 设置长按删除删除共处
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setIcon(R.mipmap.ic_launcher);
                    dialog.setTitle("是否确定删除选中的市/区?");
                    dialog.setMessage("点击确定将删除市/区：" + currentCity.getCityName());
                    // 点击确认按钮将当前城市从数据库移除
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LitePal.deleteAll(City.class, "provinceName = ? and cityName = ?",
                                    currentProvince.getProvinceName(), currentCity.getCityName());
                            cities.remove(currentCity);
                            notifyDataSetChanged();
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    return true;
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
                    List<City> filteredList;
                    if(TextUtils.isEmpty(charString)) {
                        filteredList = mSourceList;
                    } else {
                        filteredList = new ArrayList<>();
                        for(City city : mSourceList) {
                            String str = city.getCityName();
                            if(str.contains(charString)) {
                                filteredList.add(city);
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
                    mFilterList = (ArrayList<City>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    private void parseCityFromWeb(final Province currentProvince) {
        HttpUtils.sendOKHttpRequest(currentProvince.getCityQueryUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                HtmlParseUtils.extractCitiesFromHtml(currentProvince.getProvinceName(), html);

                setListValue();
                // 通知数据集更新
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "城市列表拉取成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setListValue() {
        cities.clear();
        List<City> list = LitePal.where("provinceName = ?", currentProvince.getProvinceName()).find(City.class);
        for(City city : list) {
            cities.add(city);
        }
    }
}
