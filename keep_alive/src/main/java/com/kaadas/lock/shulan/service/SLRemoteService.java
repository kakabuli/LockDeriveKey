package com.kaadas.lock.shulan.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.kaadas.lock.shulan.KeepAliveAIDL;
import com.kaadas.lock.shulan.R;
import com.kaadas.lock.shulan.config.KeepAliveConfig;
import com.kaadas.lock.shulan.config.NotificationUtils;
import com.kaadas.lock.shulan.receive.NotificationClickReceiver;
import com.kaadas.lock.shulan.utils.SPUtils;


/**
 * 守护进程
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public class SLRemoteService extends Service {
    private SLRemoteBinder mBilder;
    private String TAG = getClass().getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return mBilder;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mBilder == null) {
//            mBilder = new SLRemoteBinder();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            /*this.bindService(new Intent(SLRemoteService.this, SLLocalService.class),
                    connection, Context.BIND_ABOVE_CLIENT);*/

            shouDefNotify();
        } catch (Exception e) {
        }
        return START_STICKY;
    }

    private void shouDefNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeepAliveConfig.CONTENT = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.CONTENT);
            KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RES_ICON, R.mipmap.ic_launcher);
            KeepAliveConfig.TITLE = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.TITLE);
            if (!TextUtils.isEmpty(KeepAliveConfig.TITLE) && !TextUtils.isEmpty( KeepAliveConfig.CONTENT)) {
                //启用前台服务，提升优先级
                Intent intent2 = new Intent(SLRemoteService.this, NotificationClickReceiver.class);
                intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                Notification notification = NotificationUtils.createNotification(SLRemoteService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                startForeground(KeepAliveConfig.FOREGROUD_NOTIFICATION_ID, notification);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unbindService(connection);
    }

    private final class SLRemoteBinder extends KeepAliveAIDL.Stub {


        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void wakeUp(String title, String discription, int iconRes) throws RemoteException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title != null || discription != null) {
                    KeepAliveConfig.CONTENT = title;
                    KeepAliveConfig.DEF_ICONS = iconRes;
                    KeepAliveConfig.TITLE = discription;
                } else {
                    KeepAliveConfig.CONTENT = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.CONTENT);
                    KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RES_ICON, R.mipmap.ic_launcher);
                    KeepAliveConfig.TITLE = SPUtils.getInstance(SLRemoteService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.TITLE);

                }
                if (KeepAliveConfig.TITLE != null && KeepAliveConfig.CONTENT != null) {
                    //启用前台服务，提升优先级
                    Intent intent2 = new Intent(SLRemoteService.this, NotificationClickReceiver.class);
                    intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                    Notification notification = NotificationUtils.createNotification(SLRemoteService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                    startForeground(KeepAliveConfig.FOREGROUD_NOTIFICATION_ID, notification);
                }
            }
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Intent remoteService = new Intent(SLRemoteService.this,
                    SLLocalService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                SLRemoteService.this.startForegroundService(remoteService);
            } else {
                SLRemoteService.this.startService(remoteService);
            }
            SLRemoteService.this.bindService(new Intent(SLRemoteService.this,
                    SLLocalService.class), connection, Context.BIND_ABOVE_CLIENT);
            PowerManager pm = (PowerManager) SLRemoteService.this.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (isScreenOn) {
                sendBroadcast(new Intent(KeepAliveConfig.KAADAS_SHULAN_ACTION_SCRREN_ON));
            } else {
                sendBroadcast(new Intent(KeepAliveConfig.KAADAS_SHULAN_ACTION_SCRREN_OFF));
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            shouDefNotify();
        }
    };
}
