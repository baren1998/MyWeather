package com.example.android.myweather.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class ImageDecodeUtils {

    public static Bitmap readBitMap(Context context, int resourceId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resourceId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static Bitmap reScale(Bitmap src, int scaleRatio) {
        int originWidth = src.getWidth();
        int originHeight = src.getHeight();
        Bitmap scaledBmp = Bitmap.createScaledBitmap(src, originWidth / scaleRatio,
                originHeight / scaleRatio, false);
        return scaledBmp;
    }
}
