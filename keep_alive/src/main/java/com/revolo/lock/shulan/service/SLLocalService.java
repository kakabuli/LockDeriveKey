package com.revolo.lock.shulan.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.revolo.lock.shulan.KeepAliveAIDL;
import com.revolo.lock.shulan.KeepAliveRunning;
import com.revolo.lock.shulan.R;
import com.revolo.lock.shulan.config.KeepAliveConfig;
import com.revolo.lock.shulan.config.NotificationUtils;
import com.revolo.lock.shulan.config.RunMode;
import com.revolo.lock.shulan.receive.NotificationClickReceiver;
import com.revolo.lock.shulan.receive.OnepxReceiver;
import com.revolo.lock.shulan.utils.LogUtils;
import com.revolo.lock.shulan.utils.SPUtils;


public class SLLocalService extends Service {
    private OnepxReceiver mOnepxReceiver;
    private ScreenStateReceiver screenStateReceiver;
    private boolean isPause = true;//控制暂停
    private MediaPlayer mediaPlayer;
    private SLLocalBinder mBilder;
    private Handler handler;
    private String TAG = getClass().getSimpleName();
    private KeepAliveRunning mKeepAliveRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("本地服务"+ "：本地服务启动成功");
        if (mBilder == null) {
//            mBilder = new SLLocalBinder();
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isPause = pm.isScreenOn();
        if (handler == null) {
            handler = new Handler();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return mBilder;
        return null;
    }

    private void play() {
        LogUtils.e(TAG+ "播放音乐");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //播放无声音乐
        KeepAliveConfig.runMode = SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RUN_MODE);
        LogUtils.e(TAG+ "运行模式：" + KeepAliveConfig.runMode);
        if (mediaPlayer == null && KeepAliveConfig.runMode == RunMode.HIGH_POWER_CONSUMPTION) {
            mediaPlayer = MediaPlayer.create(this, R.raw.novioce);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    LogUtils.e(TAG+"循环播放音乐");
                    play();
                }
            });
            play();
        }
        //像素保活
        if (mOnepxReceiver == null) {
            mOnepxReceiver = new OnepxReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KeepAliveConfig.SYSTEM_SCREEN_OFF);
        intentFilter.addAction(KeepAliveConfig.SYSTEM_SCREEN_ON);
        registerReceiver(mOnepxReceiver, intentFilter);
        //屏幕点亮状态监听，用于单独控制音乐播放
        if (screenStateReceiver == null) {
            screenStateReceiver = new ScreenStateReceiver();
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(KeepAliveConfig.REVOLO_ACTION_SCRREN_ON);
        intentFilter2.addAction(KeepAliveConfig.REVOLO_ACTION_SCRREN_OFF);
        registerReceiver(screenStateReceiver, intentFilter2);

        //开启一个前台通知，用于提升服务进程优先级
        shouDefNotify();
        //绑定守护进程
        try {
//            Intent intent3 = new Intent(this, SLRemoteService.class);
//            this.bindService(intent3, connection, Context.BIND_ABOVE_CLIENT);
        } catch (Exception e) {

        }
        //隐藏服务通知
        try {
            if (Build.VERSION.SDK_INT < 25) {
                startService(new Intent(this, HideForegroundService.class));
            }
        } catch (Exception e) {

        }
        if (mKeepAliveRunning == null)
            mKeepAliveRunning = new KeepAliveRunning();
        mKeepAliveRunning.onRunning(this);
        return START_STICKY;
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getAction().equals(KeepAliveConfig.REVOLO_ACTION_SCRREN_OFF)) {
                isPause = false;
                play();
            } else if (intent.getAction().equals(KeepAliveConfig.REVOLO_ACTION_SCRREN_ON)) {
                isPause = true;
                pause();
            }
        }
    }

    private final class SLLocalBinder extends KeepAliveAIDL.Stub{

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void wakeUp(String title, String discription, int iconRes) throws RemoteException {
            shouDefNotify();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try{
                Intent remoteService = new Intent(SLLocalService.this,
                        SLRemoteService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    SLLocalService.this.startForegroundService(remoteService);
                } else {
                    SLLocalService.this.startService(remoteService);
                }
                Intent intent = new Intent(SLLocalService.this, SLRemoteService.class);
                SLLocalService.this.bindService(intent, connection,
                        Context.BIND_ABOVE_CLIENT);
                PowerManager pm = (PowerManager) SLLocalService.this.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isScreenOn();
                if (isScreenOn) {
                    sendBroadcast(new Intent(KeepAliveConfig.REVOLO_ACTION_SCRREN_ON));
                } else {
                    sendBroadcast(new Intent(KeepAliveConfig.REVOLO_ACTION_SCRREN_OFF));
                }
            }catch (Exception e){

            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                if (mBilder != null && KeepAliveConfig.foregroundNotification != null) {
                    KeepAliveAIDL guardAidl = KeepAliveAIDL.Stub.asInterface(service);
                    guardAidl.wakeUp(SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.TITLE),
                            SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.CONTENT),
                            SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RES_ICON));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unbindService(connection);
        if(mOnepxReceiver != null){
            unregisterReceiver(mOnepxReceiver);
        }
        if(screenStateReceiver != null){
            unregisterReceiver(screenStateReceiver);
        }

        if (mKeepAliveRunning != null) {
            mKeepAliveRunning.onStop();
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    private void shouDefNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                KeepAliveConfig.CONTENT = SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.CONTENT) + "";
                KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getInt(KeepAliveConfig.RES_ICON, R.mipmap.ic_launcher);
                KeepAliveConfig.TITLE = SPUtils.getInstance(SLLocalService.this, KeepAliveConfig.SP_NAME).getString(KeepAliveConfig.TITLE) + "";
            }catch (Exception e){
            }
            if (!TextUtils.isEmpty(KeepAliveConfig.TITLE) && !TextUtils.isEmpty(KeepAliveConfig.CONTENT)) {
                //启用前台服务，提升优先级
                Intent intent2 = new Intent(SLLocalService.this, NotificationClickReceiver.class);
                intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                Notification notification = NotificationUtils.createNotification(SLLocalService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                startForeground(KeepAliveConfig.FOREGROUD_NOTIFICATION_ID, notification);
            }
        }
    }
}
