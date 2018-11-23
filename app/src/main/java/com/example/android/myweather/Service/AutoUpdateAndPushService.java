package com.example.android.myweather.Service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.example.android.myweather.R;
import com.example.android.myweather.Util.HtmlParseUtils;
import com.example.android.myweather.Util.HttpUtils;
import com.example.android.myweather.Weather.Forecast;
import com.example.android.myweather.Weather.Weather;
import com.example.android.myweather.WeatherActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateAndPushService extends Service {
    public AutoUpdateAndPushService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        // 更新天气缓存
        updateWeatherPreferences();

        // 获取当前天气对象
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherJSON = preferences.getString("weather", null);
        if(weatherJSON != null) {
            // 将JSON转化为Weather对象
            Weather weather = JSON.parseObject(weatherJSON, Weather.class);

            // 获取明日预报信息
            Forecast forecastTomorrow = weather.getForecastList().get(0);

            // 弹出通知显示明日天气
            Intent intent = new Intent(this, WeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

            StringBuilder builder = new StringBuilder();
            builder.append("气温：" + forecastTomorrow.getDegree()).append("\n")
                    .append("天气状况：" + forecastTomorrow.getCondition() + "  ").append("空气质量：" + forecastTomorrow.getAqi()).append("\n")
                    .append("风向：" + forecastTomorrow.getWindDirection() + "  ").append("风力：" + forecastTomorrow.getWind());

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String id = "my_channel_01";
            NotificationChannel mChannel = null;//创建Notification Channel对象
            //如果版本号为8.0以上,定义Notification Channel
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mChannel = new NotificationChannel(id,"my_channel", NotificationManager.IMPORTANCE_DEFAULT);//设置唯一的渠道通知Id
                mChannel.enableLights(true);//开启灯光
                manager.createNotificationChannel(mChannel);//在NotificationManager中注册渠道通知对象
            }
            //定义通知,都可适配
            NotificationCompat.Builder notification=new NotificationCompat.Builder(this,id);
            notification.setContentTitle("明日天气：")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(builder.toString()))
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_MAX)//悬浮通知
                    .setContentIntent(pi)
                    .setAutoCancel(true);
            manager.notify(1,notification.build());
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int tenSeconds = 4 * 60 * 60 * 1000; // 4小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + tenSeconds;
        Intent restartServiceIntent = new Intent(this, AutoUpdateAndPushService.class);
        PendingIntent pi1 = PendingIntent.getService(this, 0, restartServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pi1);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi1);

        return START_STICKY;
    }

    private void updateWeatherPreferences() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherQueryUrl = preferences.getString("weatherQuery", null);
        if(weatherQueryUrl != null) {
            // 去服务器请求新的数据存入缓存并弹出通知告知明日天气
            HttpUtils.sendOKHttpRequest(weatherQueryUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String htmlData = response.body().string();
                    Weather weather = HtmlParseUtils.handleWeatherResponse(htmlData);
                    String weatherJSON = JSON.toJSONString(weather);
                    // 将新的天气JSON串存入缓存
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("weather", weatherJSON);
                    editor.apply();
                }
            });
        }
    }
}
