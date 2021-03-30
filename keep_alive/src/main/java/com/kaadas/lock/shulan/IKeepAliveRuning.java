package com.kaadas.lock.shulan;

import android.content.Context;

public interface IKeepAliveRuning {
    void onRunning(Context context);
    void onStop();
}
