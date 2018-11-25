package com.example.android.myweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.myweather.Util.HtmlParseUtils;
import com.example.android.myweather.Util.HttpUtils;
import com.example.android.myweather.db.Province;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseProvinceFragment extends Fragment {
    private final String[] totalKeys = {"A", "B", "C", "F", "G", "H", "J", "L", "N", "Q", "S", "T", "X", "Y", "Z"};

    private List<String> currentKeys;
    private ArrayMap<String, List<Province>> map;

    private RecyclerView recyclerView;
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabRefresh;
    private AlertDialog dialog;
    private TextInputLayout inputLayout;
    private EditText editText;
    private Button commitBtn;
    private Button cancelBtn;

    private ProvincesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_province_frag, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.choose_area_view);

        currentKeys = new ArrayList<>();
        map = new ArrayMap<>();
        adapter = new ProvincesListAdapter(getActivity(), currentKeys, map);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        // 初始化AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View customDialogView = getActivity().getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setCancelable(true);
        inputLayout = (TextInputLayout) customDialogView.findViewById(R.id.input_wrapper);
        inputLayout.setHint("请输入想添加的省份的拼音");
        editText = (EditText) customDialogView.findViewById(R.id.edit_text);
        commitBtn = (Button) customDialogView.findViewById(R.id.commit_btn);
        cancelBtn = (Button) customDialogView.findViewById(R.id.cancel_btn);
        builder.setView(customDialogView);
        dialog = builder.create();

        // 设置alertdiag按钮的监听器
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String provinceName = editText.getText().toString();
                addProvince(provinceName);
                dialog.dismiss();
            }
        });

        // 初始化浮动菜单和按钮
        fabMenu = (FloatingActionMenu) view.findViewById(R.id.fab_menu);
        fabMenu.setClosedOnTouchOutside(true);

        fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add_province);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                dialog.show();
                fabMenu.close(true);
            }
        });

        fabRefresh = (FloatingActionButton) view.findViewById(R.id.fab_refresh_all);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseProvinceFromWeb();
                fabMenu.close(true);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LitePal.getDatabase();
        // 去数据库上查询省份列表，如果没有则墨迹天气网页上爬取数据并解析
        List<Province> provinces = LitePal.findAll(Province.class);
        if(provinces.size() == 0) {
            parseProvinceFromWeb();
        } else {
            setMapValue();
            adapter.notifyDataSetChanged();
        }
    }

    /* 从数据库查询并设置ArrayMap的值 */
    private void setMapValue() {
        clearMap();
        for(int i = 0; i < totalKeys.length; i++) {
            String currentKey = totalKeys[i];
            // 查找当前关键字对应的省份列表
            List<Province> currentProvinces = LitePal.where("key = ?", currentKey)
                    .find(Province.class);
            // 如果查询结果不为空，则将关键字的对应省份列表加入List和ArrayMap
            if(currentProvinces.size() != 0) {
                currentKeys.add(currentKey);
                map.put(currentKey, currentProvinces);
            }
        }
    }

    private void clearMap() {
        currentKeys.clear();
        map.clear();
    }

    /* 爬取并解析墨迹天气的省市数据并存入数据库 */
    private void parseProvinceFromWeb() {

        HttpUtils.sendOKHttpRequest("https://tianqi.moji.com/weather/china", new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String htmlData = response.body().string();
                HtmlParseUtils.ParseProvincesFromHtml(htmlData);

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

    private void addProvince(final String provinceName) {
        StringBuilder builder = new StringBuilder("https://tianqi.moji.com/weather/china/");
        builder.append(provinceName);
        final String queryUrl = builder.toString();
        HttpUtils.sendOKHttpRequest(queryUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getActivity(), "获取省份失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String htmlData = response.body().string();
                if(htmlData.contains("您要查看的网址可能已被删除或者暂时不可用")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "获取省份失败，请检查省份拼写", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    final String name = HtmlParseUtils.extractNameFromHtml(htmlData);

                    List<Province> p = LitePal.where("provinceName = ?", name).find(Province.class);
                    if(p.size() != 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "该省份已存在，不可重复添加", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        String key = provinceName.substring(0, 1).toUpperCase();

                        Province province = new Province();
                        province.setKey(key);
                        province.setProvinceName(name);
                        province.setCityQueryUrl(queryUrl);
                        province.save();

                        // 刷新RecyclerView数据
                        setMapValue();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "添加省份：" + name, Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }
}