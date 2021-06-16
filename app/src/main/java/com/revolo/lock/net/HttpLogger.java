package com.revolo.lock.net;

import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * author :
 * time   : 2021/2/1
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger {

    @Override
    public void log(String message) {
        Timber.d(message);//okHttp的详细日志会打印出来
    }
}
