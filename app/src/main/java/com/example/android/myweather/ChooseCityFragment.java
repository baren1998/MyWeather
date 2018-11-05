package com.example.android.myweather;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.myweather.Util.HttpUtil;

import org.jsoup.Jsoup;
import org.jsoup.select.Evaluator;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseCityFragment extends Fragment {

    String queryCityUrl;
    RecyclerView cityList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_city_frag, container, false);
        cityList = (RecyclerView) view.findViewById(R.id.city_list);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> {

        public CityListAdapter() {}

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView cityText;

            public ViewHolder(View view) {
                cityText = (TextView) view.findViewById(R.id.city_text);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private void ParseCityFromWeb() {
        HttpUtil.sendOKHttpRequest(queryCityUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
