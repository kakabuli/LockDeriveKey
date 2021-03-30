package com.kaadas.lock.shulan.receive;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.kaadas.lock.shulan.config.KeepAliveConfig;
import com.kaadas.lock.shulan.onepx.OnePixelActivity;
import com.kaadas.lock.shulan.utils.KeepAliveUtils;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public final class OnepxReceiver extends BroadcastReceiver {
    android.os.Handler mHander;
    boolean appIsForeground = false;

    public OnepxReceiver() {
        mHander = new android.os.Handler(Looper.getMainLooper());
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {    //屏幕关闭的时候接受到广播
            appIsForeground = KeepAliveUtils.IsForeground(context);
            try {
                Intent it = new Intent(context, OnePixelActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(it);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //通知屏幕已关闭，开始播放无声音乐
            context.sendBroadcast(new Intent(KeepAliveConfig.KAADAS_SHULAN_ACTION_SCRREN_OFF));
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {   //屏幕打开的时候发送广播  结束一像素
            context.sendBroadcast(new Intent(KeepAliveConfig.ONE_PIXEL_FINISH_ACTIVITY));
            if (!appIsForeground) {
                appIsForeground = false;
                try {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    home.addCategory(Intent.CATEGORY_HOME);
                    context.getApplicationContext().startActivity(home);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //通知屏幕已点亮，停止播放无声音乐
            context.sendBroadcast(new Intent(KeepAliveConfig.KAADAS_SHULAN_ACTION_SCRREN_ON));
        }
    }


}