package com.kaadas.lock.shulan.service;


import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.kaadas.lock.shulan.config.KeepAliveConfig;
import com.kaadas.lock.shulan.config.NotificationUtils;
import com.kaadas.lock.shulan.receive.NotificationClickReceiver;
import com.kaadas.lock.shulan.utils.SPUtils;

/**
 * 隐藏前台服务通知
 */
public class HideForegroundService extends Service {
    private Handler handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground();
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopForeground(true);
                stopSelf();
            }
        }, 2000);
        return START_NOT_STICKY;
    }


    private void startForeground() {
        if (KeepAliveConfig.foregroundNotification != null) {
            Intent intent = new Intent(HideForegroundService.this, NotificationClickReceiver.class);
            intent.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
            Notification notification = NotificationUtils.createNotification(this,
                    SPUtils.getInstance(HideForegroundService.this,KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.TITLE),
                    SPUtils.getInstance(HideForegroundService.this,KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.CONTENT),
                    SPUtils.getInstance(HideForegroundService.this,KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RES_ICON),
                    intent
            );
            startForeground(KeepAliveConfig.FOREGROUD_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
