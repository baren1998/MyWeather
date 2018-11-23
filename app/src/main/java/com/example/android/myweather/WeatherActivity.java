package com.example.android.myweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.android.myweather.Service.AutoUpdateAndPushService;
import com.example.android.myweather.Util.HtmlParseUtils;
import com.example.android.myweather.Util.HttpUtils;
import com.example.android.myweather.Util.ImageDecodeUtils;
import com.example.android.myweather.Util.ShareUtils;
import com.example.android.myweather.Weather.Forecast;
import com.example.android.myweather.Weather.ForecastSeven;
import com.example.android.myweather.Weather.LiveIndex;
import com.example.android.myweather.Weather.Weather;
import com.example.android.myweather.db.City;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.xujiaji.happybubble.BubbleDialog;
import com.xujiaji.happybubble.BubbleLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private String cityName;
    private String weatherQueryUrl;
    private Weather weather;

    // title.xml中的控件
    private TextView titleCity;
    private TextView titleUpdateTime;
    private Button navButton;

    // now.xml中的控件
    private TextView currentDegreeText;
    private TextView currentConditionText;
    private TextView humidityText;
    private TextView currentWindText;
    private TextView weatherTipsText;
    private ImageView currentConditionImg;

    // aqi.xml中的控件
    private TextView currentAqiText;
    private TextView pm25Text;

    // live_index_grid.xml的相关控件
    private GridView liveIndexGrid;

    // layout相关控件
    private LinearLayout forecastLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FrameLayout weatherFrameLayout;

    // LiveIndexAdapter对象及其数据源
    private LiveIndexAdapter adapter;
    private List<LiveIndex> mLiveIndexList;

    private TextView threeDaysBtn;
    private TextView sevenDaysBtn;

    private ProgressBar loadingSpinner;
    private ImageView weatherBgImg;
    private ImageView blurBgImg;

    // 浮动菜单和按钮的功能
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabRefresh;
    private FloatingActionButton fabShare;

    // 分享功能所有截图的文件路径列表
    private List<String> screenShotPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        // 初始化各控件
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);

        currentDegreeText = findViewById(R.id.current_degree);
        currentConditionText = findViewById(R.id.current_condition);
        humidityText = findViewById(R.id.humidity);
        currentWindText = findViewById(R.id.current_wind);
        weatherTipsText = findViewById(R.id.weather_tips);
        currentConditionImg = findViewById(R.id.current_condition_img);

        currentAqiText = findViewById(R.id.current_aqi_text);
        pm25Text = findViewById(R.id.pm25_text);

        liveIndexGrid = findViewById(R.id.live_index_grid);

        forecastLayout = findViewById(R.id.forecast_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        weatherFrameLayout = findViewById(R.id.weather_frame_layout);

        loadingSpinner = findViewById(R.id.loading_spinner);
        weatherBgImg = findViewById(R.id.weather_background);
        blurBgImg = findViewById(R.id.blur_background);

        screenShotPaths = new ArrayList<>();
        mLiveIndexList = new ArrayList<>();
        adapter = new LiveIndexAdapter(this, R.layout.live_index_item, mLiveIndexList);
        liveIndexGrid.setAdapter(adapter);

        // 设置点击生活指数单项时产生的气泡对话框
        liveIndexGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                LiveIndex liveIndex = mLiveIndexList.get(position);
                View bubbleView = LayoutInflater.from(WeatherActivity.this)
                        .inflate(R.layout.bubble_dialog, null);
                TextView liveIndexTips = bubbleView.findViewById(R.id.tips);
                liveIndexTips.setText(liveIndex.getTips());

                BubbleLayout bubbleLayout = new BubbleLayout(WeatherActivity.this);
                bubbleLayout.setBubbleColor(Color.argb(127, 255, 255, 255));

                new BubbleDialog(WeatherActivity.this)
                        .addContentView(bubbleView)
                        .setClickedView(view)
                        .setOffsetY(24)
                        .setBubbleLayout(bubbleLayout)
                        .calBar(true)
                        .show();
            }
        });

        threeDaysBtn = findViewById(R.id.three_days_btn);
        sevenDaysBtn = findViewById(R.id.seven_days_btn);
        //设置天气预报栏的按功能
        threeDaysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadForecast();
            }
        });

        sevenDaysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadForecastSeven();
            }
        });

        // 设置下拉刷新时重新去网站获取数据
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherQueryUrl);
            }
        });

        // 设置按钮功能为跳转到城市挑选界面
        navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 设置浮动菜单和按钮的功能
        fabMenu = findViewById(R.id.weather_fab_menu);
        fabMenu.setClosedOnTouchOutside(true);

        fabRefresh = findViewById(R.id.weather_refresh_fab);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                // 获取当前屏幕截图并虚化，设置为虚化背景
                Bitmap screenShot = ShareUtils.getScreenShot(weatherFrameLayout);
                Bitmap scaledBmp = ImageDecodeUtils.reScale(screenShot, 20);
                Blurry.with(WeatherActivity.this).radius(8).from(scaledBmp).into(blurBgImg);
                blurBgImg.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);

                requestWeather(weatherQueryUrl);
            }
        });

        fabShare = findViewById(R.id.weather_share_fab);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                // 在调用分享功能前先检查是否具有运行时权限
                if(ContextCompat.checkSelfPermission(WeatherActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(WeatherActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                } else {
                    // 调用分享功能，并获得截图在相册中存储的uri
                    Uri imgUri = ShareUtils.shareToOtherApp(WeatherActivity.this, weatherFrameLayout);
                    // 根据uri获取该图片文件的路径
                    String imgPath = ShareUtils.getRealPathFromURI(WeatherActivity.this, imgUri);
                    // 将该文件路径存入列表
                    screenShotPaths.add(imgPath);
                    // 判断，如果当前列表有大于2张截图，则删除前一张，暂时保留当前截图完成分享
                    if(screenShotPaths.size() >= 2) {
                        String lastImgPath = screenShotPaths.remove(0);
                        // 删除图片文件
                        ShareUtils.deleteImage(WeatherActivity.this, lastImgPath);
                        // 通知媒体库更新
                        ShareUtils.updateMediaStore(WeatherActivity.this, lastImgPath);
                    }
                }
            }
        });

        // 读取缓存信息，加载数据
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityName = preferences.getString("city", null);
        if(cityName != null) {
            weatherQueryUrl = preferences.getString("weatherQuery", null);
            String weatherJSON = preferences.getString("weather", null);
            weather = JSON.parseObject(weatherJSON, Weather.class);
            showWeatherInfo();
        }

        // 开启更新与推送服务
        Intent intent = new Intent(this, AutoUpdateAndPushService.class);
        startService(intent);

        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 接触EventBus注册
        EventBus.getDefault().unregister(this);
        // 删除截图列表中的最后一张图片
        String lastImgPath = screenShotPaths.remove(0);
        ShareUtils.deleteImage(WeatherActivity.this, lastImgPath);
        ShareUtils.updateMediaStore(WeatherActivity.this, lastImgPath);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleCity(City event) {
        cityName = event.getCityName();
        weatherQueryUrl = event.getQueryWeatherUrl();

        loadingSpinner.setVisibility(View.VISIBLE);

        // 获取当前屏幕截图并虚化，设置为虚化背景
        Bitmap screenShot = ShareUtils.getScreenShot(weatherFrameLayout);
        Bitmap scaledBmp = ImageDecodeUtils.reScale(screenShot, 20);
        Blurry.with(WeatherActivity.this).radius(8).from(scaledBmp).into(blurBgImg);
        blurBgImg.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);

        requestWeather(weatherQueryUrl);
        // 移除该黏性事件，防止重复接收
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShareUtils.shareToOtherApp(WeatherActivity.this, weatherFrameLayout);
                } else {
                    Toast.makeText(this, "你拒绝了权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /* 从网站上获取需要的天气信息 */
    private void requestWeather(String queryWeatherUrl) {

        HttpUtils.sendOKHttpRequest(queryWeatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        blurBgImg.setVisibility(View.GONE);
                        loadingSpinner.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String html = response.body().string();
                final Weather weatherResponse = HtmlParseUtils.handleWeatherResponse(html);
                weather = weatherResponse;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 将获得的weather使用转换为JSON字符串后使用sharedpreferences保存
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("city", cityName);
                        editor.putString("weatherQuery", weatherQueryUrl);

                        String weatherJSON = JSON.toJSONString(weather, true);
                        editor.putString("weather", weatherJSON);
                        editor.apply();

                        // 展示天气数据
                        blurBgImg.setVisibility(View.GONE);
                        loadingSpinner.setVisibility(View.GONE);
                        showWeatherInfo();
                        Toast.makeText(WeatherActivity.this, "天气信息获取成功", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }


    /* 处理并展示天气实体类中的数据 */
    private void showWeatherInfo() {
         // 显示title中的数据
        String updateTime = weather.getUpdateTime();
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);

        // 显示now中的数据
        String currentDegree = weather.getCurrentDegree();
        String currentConditionImgUrl = weather.getCurrentConiditionImgUrl();
        String currentCondition = weather.getCurrentCondition();
        String humidity = weather.getHumidity();
        String currentWind = weather.getCurrentWind();
        String weatherTips = weather.getWeatherTips();
        currentDegreeText.setText(currentDegree);
        currentConditionText.setText(currentCondition);
        humidityText.setText(humidity);
        currentWindText.setText(currentWind);
        weatherTipsText.setText(weatherTips);

        // 根据当前天气状况设置天气背景（实现简易版的app主题变换)
        Bitmap weatherBgBmp = null;
        if(currentCondition.equals("晴")) {
            weatherBgBmp = ImageDecodeUtils.readBitMap(WeatherActivity.this, R.drawable.weather_sunny);
        }
        else if(currentCondition.contains("阴") || currentCondition.contains("多云")) {
            weatherBgBmp = ImageDecodeUtils.readBitMap(WeatherActivity.this, R.drawable.weather_cloudy);
        }
        else if(currentCondition.contains("雨")) {
            weatherBgBmp = ImageDecodeUtils.readBitMap(WeatherActivity.this, R.drawable.weather_rain);
        }
        else if(currentCondition.contains("雪")) {
            weatherBgBmp = ImageDecodeUtils.readBitMap(WeatherActivity.this, R.drawable.weather_snowy);
        }
        // 设置背景图片，实现简易的主题改变
        weatherBgImg.setImageBitmap(weatherBgBmp);

        // 加载图片
        loadPic(currentConditionImgUrl, currentConditionImg);

        // 显示aqi中的数据
        String currentAqi = weather.getCurrentAqi();
        String pm25 = weather.getPm25();
        currentAqiText.setText(currentAqi);
        pm25Text.setText(pm25);

        // 显示forecast中的数据(默认为3天)
        loadForecast();

        // 绑定GirdView中的数据
        mLiveIndexList.clear();
        List<LiveIndex> liveIndices = weather.getLiveIndexList();
        for(LiveIndex liveIndex : liveIndices) {
            mLiveIndexList.add(liveIndex);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadPic(final String imgUrl, final ImageView targetView) {
        Glide.with(WeatherActivity.this).load(imgUrl).into(targetView);
    }

    private void loadForecast() {
        // 显示forecast中的数据
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.getForecastList()) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dayText = view.findViewById(R.id.day_text);
            ImageView conditionImg = view.findViewById(R.id.condition_img);
            TextView condition = view.findViewById(R.id.condition_text);
            TextView degree = view.findViewById(R.id.degree_text);
            TextView windDirection = view.findViewById(R.id.wind_direction_text);
            TextView wind = view.findViewById(R.id.wind_text);
            TextView aqi = view.findViewById(R.id.aqi_text);

            dayText.setText(forecast.getDay());
            condition.setText(forecast.getCondition());
            degree.setText(forecast.getDegree());
            windDirection.setText(forecast.getWindDirection());
            wind.setText(forecast.getWind());
            aqi.setText(forecast.getAqi());
            // 判断aqi处的背景颜色
            int aqiValue = Integer.parseInt(forecast.getAqi().substring(0, forecast.getAqi().indexOf(" ")));
            if(aqiValue <= 50) {
                aqi.setBackgroundResource(R.drawable.rounded_rectangle_green);
            } else {
                aqi.setBackgroundResource(R.drawable.rounded_rectangle_orange);
            }
            forecastLayout.addView(view);
            // 加载图片
            loadPic(forecast.getConditionImgUrl(), conditionImg);
        }
    }

    private void loadForecastSeven() {
        // 将forecastlayout中的数据改为七日预报
        forecastLayout.removeAllViews();
        for(ForecastSeven forecastSeven : weather.getForecastSevenList()) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_seven_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView weekText = view.findViewById(R.id.week_text);
            ImageView sfConditionImg = view.findViewById(R.id.sf_condition_img);
            TextView sfConditionText = view.findViewById(R.id.sf_condition_text);
            TextView minText = view.findViewById(R.id.min_text);
            TextView maxText = view.findViewById(R.id.max_text);

            dateText.setText(forecastSeven.getDate());
            weekText.setText(forecastSeven.getWeek());
            sfConditionText.setText(forecastSeven.getCondition());
            minText.setText(forecastSeven.getMin());
            maxText.setText(forecastSeven.getMax());

            forecastLayout.addView(view);
            // 加载图片
            loadPic(forecastSeven.getConditionImgUrl(), sfConditionImg);
        }
    }
}
