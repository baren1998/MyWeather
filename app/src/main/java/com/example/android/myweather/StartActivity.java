package com.example.android.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String cityName = preferences.getString("city", null);
        if(cityName == null) {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(StartActivity.this, WeatherActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
