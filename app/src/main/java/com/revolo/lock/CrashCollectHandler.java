package com.revolo.lock;

import android.content.Context;

import androidx.annotation.NonNull;

public class CrashCollectHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static volatile CrashCollectHandler mInstance;

    private CrashCollectHandler() {

    }

    public static CrashCollectHandler getInstance() {
        synchronized (CrashCollectHandler.class) {
            if (mInstance == null) {
                mInstance = new CrashCollectHandler();
            }
            return mInstance;
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (e != null && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        } else {
            LockAppManager.getAppManager().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    public void init(Context context) {
        this.mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
}
