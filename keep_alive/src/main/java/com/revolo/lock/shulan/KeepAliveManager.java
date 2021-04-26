package com.revolo.lock.shulan;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.revolo.lock.shulan.config.ForegroundNotification;
import com.revolo.lock.shulan.config.KeepAliveConfig;
import com.revolo.lock.shulan.config.NotificationUtils;
import com.revolo.lock.shulan.config.RunMode;
import com.revolo.lock.shulan.service.HideForegroundService;
import com.revolo.lock.shulan.service.JobHandlerService;
import com.revolo.lock.shulan.service.SLLocalService;
import com.revolo.lock.shulan.service.SLRemoteService;
import com.revolo.lock.shulan.utils.KeepAliveUtils;
import com.revolo.lock.shulan.utils.LogUtils;
import com.revolo.lock.shulan.utils.SPUtils;


/**
 * 进程保活管理
 */
public class KeepAliveManager {
    private static final String TAG = "KeepAliveManager";

    private KeepAliveManager() {}

    /**
     * 启动保活
     *
     * @param application            your application
     * @param runMode
     * @param foregroundNotification 前台服务
     */
    public static void toKeepAlive(@NonNull Application application, @NonNull int runMode, String title, String content, int res_icon, ForegroundNotification foregroundNotification) {
        if (KeepAliveUtils.isRunning(application)) {
            KeepAliveConfig.foregroundNotification = foregroundNotification;
            SPUtils.getInstance(application, KeepAliveConfig.SP_NAME).put(KeepAliveConfig.TITLE, title);
            SPUtils.getInstance(application, KeepAliveConfig.SP_NAME).put(KeepAliveConfig.CONTENT, content);
            SPUtils.getInstance(application, KeepAliveConfig.SP_NAME).put(KeepAliveConfig.RES_ICON, res_icon);
            SPUtils.getInstance(application, KeepAliveConfig.SP_NAME).put(KeepAliveConfig.RUN_MODE, runMode);
            //优化后的枚举
            RunMode.setShape(runMode);
            KeepAliveConfig.runMode = RunMode.getShape();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //启动定时器，在定时器中启动本地服务和守护进程
                JobHandlerService.startJob(application);
            } else {
                Intent localIntent = new Intent(application, SLLocalService.class);
                //启动守护进程
                Intent guardIntent = new Intent(application, SLRemoteService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    application.startForegroundService(localIntent);
//                    application.startForegroundService(guardIntent);
                } else {
                    application.startService(localIntent);
//                    application.startService(guardIntent);
                }
            }
        }
    }

    public static void hideForegroudService(Application application){
        //隐藏服务通知
        try {
            if (Build.VERSION.SDK_INT < 25) {
                application.startService(new Intent(application, HideForegroundService.class));
            }
        } catch (Exception e) {
            LogUtils.e("HideForegroundService--", e.getMessage());

        }
    }

    public static void stopWork(Application application) {
        try {
            KeepAliveConfig.foregroundNotification = null;
            KeepAliveConfig.keepLiveService = null;
            KeepAliveConfig.runMode = RunMode.getShape();
            JobHandlerService.stopJob();
            //启动本地服务
            Intent localIntent = new Intent(application, SLLocalService.class);
            //启动守护进程
            Intent guardIntent = new Intent(application, SLRemoteService.class);
            application.stopService(localIntent);
//            application.stopService(guardIntent);
            application.stopService(new Intent(application, JobHandlerService.class));
        } catch (Exception e) {
            LogUtils.e(TAG + "stopWork-->" + e.getMessage());
        }
    }


    public static void sendNotification(Context context, String title, String content, int icon, Intent intent2) {
        NotificationUtils.sendNotification(context, title, content, icon, intent2);
    }

    /**
     * 启动系统保活
     * @param cex
     */
    public static void launcherSyskeepAlive(Context cex){
        DevicesLaunchConfig.launchSystemKeepAlive(cex);
    }

    /**
     * 启动电量优化
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void batteryOptimizations(Context context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            requestIgnoreBatteryOptimizations(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
