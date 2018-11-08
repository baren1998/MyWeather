package com.example.android.myweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.myweather.db.Province;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.List;

public class ProvincesListAdapter extends RecyclerView.Adapter<ProvincesListAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mKeys;
    private ArrayMap<String, List<Province>> mMap;
    private AlertDialog dialog;

    public ProvincesListAdapter(Context context, List<String> keys, ArrayMap<String, List<Province>> map) {
        mKeys = keys;
        mMap = map;
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyText;
        RecyclerView provinceList;

        public ViewHolder(View view) {
            super(view);
            keyText = (TextView) view.findViewById(R.id.key_text);
            provinceList = (RecyclerView) view.findViewById(R.id.provinces_list);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.provinces_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前项的关键字
        String key = mKeys.get(position);
        holder.keyText.setText(key);

        // 根据关键字提取省份List
        List<Province> provinces = mMap.get(key);

        ProvinceItemAdapter adapter = new ProvinceItemAdapter(provinces);
        holder.provinceList.setHasFixedSize(true);

        // 为省份子Recyclerview设置网格布局管理器
        holder.provinceList.setLayoutManager(new GridLayoutManager(mContext, 4));
        holder.provinceList.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return mKeys.size();
    }

    /* 定义省份item适配器 */
    private class ProvinceItemAdapter extends RecyclerView.Adapter<ProvinceItemAdapter.ViewHolder> {

        private List<Province> mProvinces;

        public ProvinceItemAdapter(List<Province> provinces) {
            mProvinces = provinces;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView provinceText;

            public ViewHolder(View view) {
                super(view);
                provinceText = (TextView) view.findViewById(R.id.province_text);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.provinces_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            final Province province = mProvinces.get(position);
            holder.provinceText.setText(province.getProvinceName());
            // 设置点击监听器，跳转到对应省份的城市列表界面
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Province province1 = mProvinces.get(position);
                    EventBus.getDefault().post(province1.getCityQueryUrl());
                }
            });
            // 设置长按监听实现删除功能
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // 设置AlertDialog询问是否执行删除操作
                    dialog = new AlertDialog.Builder(mContext).create();
                    dialog.setIcon(R.mipmap.ic_launcher);
                    dialog.setTitle("是否确定删除选中的省份？");
                    dialog.setMessage("点击确定将删除省份：" + province.getProvinceName());
                    // 点击确定按钮后将当前省份从数据库移除
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LitePal.deleteAll(Province.class, "provinceName = ?", province.getProvinceName());
                            mProvinces.remove(province);
                            // 判断map中当前同关键字省份是否已经为空
                            List<Province> currentKeyProvinces = mMap.get(province.getKey());
                            currentKeyProvinces.remove(province);
                            if(currentKeyProvinces.size() == 0) {
                                mKeys.remove(province.getKey());
                                mMap.remove(province.getKey());
                            }
                            notifyDataSetChanged();
                            ProvincesListAdapter.this.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mProvinces.size();
        }
    }
}
