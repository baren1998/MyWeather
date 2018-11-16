package com.example.android.myweather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.myweather.Weather.LiveIndex;

import java.util.List;

public class LiveIndexAdapter extends ArrayAdapter<LiveIndex> {

    private Context mContext;
    private int mResourceId;
    private List<LiveIndex> mLiveIndexList;

    public LiveIndexAdapter(@NonNull Context context, int resource, @NonNull List<LiveIndex> objects) {
        super(context, resource, objects);
        this.mContext = context;
        mResourceId = resource;
        mLiveIndexList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(mResourceId, parent, false);
            holder = new ViewHolder();
            holder.img = convertView.findViewById(R.id.live_index_img);
            holder.title = convertView.findViewById(R.id.live_index_title);
            holder.comment = convertView.findViewById(R.id.live_index_comment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LiveIndex liveIndex = mLiveIndexList.get(position);
        holder.img.setImageResource(liveIndex.getImgResourceId());
        holder.title.setText(liveIndex.getTitle());
        holder.comment.setText(liveIndex.getComment());

        return convertView;
    }

    private class ViewHolder {
        ImageView img;
        TextView title;
        TextView comment;
    }
}
