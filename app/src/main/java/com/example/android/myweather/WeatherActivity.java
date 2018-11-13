package com.example.android.myweather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.android.myweather.Util.HttpUtil;
import com.example.android.myweather.db.City;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private City currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 接收黏性事件并处理
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(City event) {
        String cityName = event.getCityName();
        currentCity = event;
        Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
    }

    /*  */
    private void requestWeather() {
        HttpUtil.sendOKHttpRequest(currentCity.getQueryWeatherUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();

            }
        });
    }
}
