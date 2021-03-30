package com.kaadas.lock.shulan.config;

import com.kaadas.lock.shulan.R;

/**
 * 进程保活需要配置的属性
 */
public class KeepAliveConfig {

    /**
     * Job 的时间
     */
    public static final int JOB_TIME = 1000;
    public static final int FOREGROUD_NOTIFICATION_ID = 8888;
    /**
     * 运行模式
     */
    public static final String RUN_MODE = "RUN_MODE";
    /**
     * 进程开启的广播
     */
    public static final String PROCESS_ALIVE_ACTION = "PROCESS_ALIVE_ACTION";
    public static final String PROCESS_STOP_ACTION = "PROCESS_STOP_ACTION";
    public static ForegroundNotification foregroundNotification = null;
    public static KeepLiveService keepLiveService = null;
    public static int runMode = RunMode.getShape();

    /**
     * 广播通知的 action
     */
    public static String NOTIFICATION_ACTION = "NOTIFICATION_ACTION";
    public static String ONE_PIXEL_FINISH_ACTIVITY = "ONE_PIXEL_FINISH_ACTIVITY";
    public static String KAADAS_SHULAN_ACTION_SCRREN_OFF = "KAADAS_SHULAN_ACTION_SCRREN_OFF";
    public static String KAADAS_SHULAN_ACTION_SCRREN_ON = "KAADAS_SHULAN_ACTION_SCRREN_ON";
    public static String SYSTEM_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    public static String SYSTEM_SCREEN_ON = "android.intent.action.SCREEN_ON";

    public static String TITLE = "TITLE";
    public static String CONTENT = "CONTENT";
    public static String RES_ICON = "RES_ICON";
    public static int DEF_ICONS = R.mipmap.ic_launcher;
    public static String SP_NAME = "shulanKeepAliveConfig";

}
