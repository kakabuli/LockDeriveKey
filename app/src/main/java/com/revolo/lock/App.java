package com.revolo.lock;

import android.app.Application;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class App extends Application {

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
