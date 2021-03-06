package com.example.android.myweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.android.myweather.db.Province;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private Province currentProvince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment fragment = new ChooseProvinceFragment();
        replaceFragment(fragment);
        // 注册EventBus订阅者
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销EventBus注册
        EventBus.getDefault().unregister(this);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_layout, fragment);
        if(fragment instanceof ChooseCityFragment) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void transationEventBus(Province event) {
        currentProvince = event;
        replaceFragment(new ChooseCityFragment());
    }

    public Province getCurrentProvince() {
        return currentProvince;
    }
}
