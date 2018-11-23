package com.example.android.myweather.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareUtils {

//    public static Bitmap getActivityShot(Activity activity) {
//        View view = activity.getWindow().getDecorView();
//
//        view.buildDrawingCache();
//        view.setDrawingCacheEnabled(true);
//
//        Rect rect = new Rect();
//        view.getWindowVisibleDisplayFrame(rect);
//        int statusBarHeight = rect.top;
//
//        Display display = activity.getWindowManager().getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();
//
//        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0,
//                statusBarHeight, width, height - statusBarHeight);
//
//        view.destroyDrawingCache();
//        return bitmap;
//    }

    public static Bitmap getScreenShot(FrameLayout frameLayout) {
        // 创建对应大小的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(frameLayout.getWidth(), frameLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        frameLayout.draw(canvas);

        return bitmap;
    }

    public static Uri shareToOtherApp(Activity activity, FrameLayout frameLayout) {
//        Bitmap bitmap = getActivityShot(activity);
        Bitmap bitmap = getScreenShot(frameLayout);

        String bmpPath = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, null, null);
        Uri uri = Uri.parse(bmpPath);

        String title = "Hi";
        String text = "我向你分享了今日天气";
        String type = "image/*";
        Intent shareIntent;

        shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType(type);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        PackageManager pm = activity.getPackageManager();
        // 根据当前Intent的设定，获取设备上支持此分享的应用集合
        List<ResolveInfo> resInfo = pm.queryIntentActivities(shareIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;

            // 这里可以根据实际需要进行过滤
            if (packageName.contains("mobileqq") || packageName.contains("tencent.mm") || packageName.contains("tencent.pb")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(type);
                intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.putExtra(Intent.EXTRA_STREAM, uri);

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // 注意，这里用的remove，为的是避免第一个分享方式重复，因为后面设置了额外的方式
        Intent openInChooser = Intent.createChooser(intentList.remove(0), "请选择您要分享的方式");
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        activity.startActivity(openInChooser);
        return uri;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result = null;
        // 如果为content类型，则根据uri查询图片路径
        if(contentURI.getScheme().equalsIgnoreCase("content")) {
            Cursor cursor = context.getContentResolver().query(contentURI,
                    new String[]{MediaStore.Images.ImageColumns.DATA},//
                    null, null, null);
            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    result = cursor.getString(index);
                }
                cursor.close();
            }
        }
        // 如果为文件类型，则直接获取文件路径
        else if(contentURI.getScheme().equalsIgnoreCase("file")) {
            result = contentURI.getPath();
        }

        return result;
    }

    public static void deleteImage(Context context, String filePath) {
        if(filePath != null) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();
            String where = MediaStore.Images.Media.DATA + "='" + filePath + "'";
            //删除图片
            mContentResolver.delete(uri, where, null);
        }
    }

    public static void updateMediaStore(final  Context context, final String path) {
        //版本号的判断  4.4为分水岭，发送广播更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    context.sendBroadcast(mediaScanIntent);
                }
            });
        } else {
            File file = new File(path);
            String relationDir = file.getParent();
            File file1 = new File(relationDir);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
        }
    }
}
