package com.kaadas.lock.shulan;


import android.content.Context;
import android.content.Intent;

import com.kaadas.lock.shulan.utils.KeepAliveUtils;
import com.kaadas.lock.shulan.utils.LogUtils;

public class KeepAliveRunning implements IKeepAliveRuning {


    @Override
    public void onRunning(Context context) {

        LogUtils.e("runing?KeepAliveRuning", "true");

        ClassLoader classLoader = KeepAliveRunning.class.getClassLoader();
        Class<?> clz;
        try {
            // TODO: 2021/3/30 要保活的类
            clz = classLoader.loadClass("com.revolo.lock.ui.MainActivity");
            LogUtils.e("shulan 0000000 " + clz.getName());
            Intent intent = new Intent(context, clz);
            if(!KeepAliveUtils.isServiceRunning(context,clz.getName()))
                context.startService(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onStop() {
        LogUtils.e("runing?KeepAliveRuning", "false");
    }
}
