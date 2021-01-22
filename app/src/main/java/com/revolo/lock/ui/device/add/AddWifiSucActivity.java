package com.revolo.lock.ui.device.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import java.util.List;

/**
 * author :
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AddWifiSucActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi_suc;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
    }

    @Override
    public void doBusiness() {
        finishPreActivities();
        threeSecFinish();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
    }

    private void finishPreActivities() {
        List<Activity> activities = ActivityUtils.getActivityList();
        if(activities.isEmpty()) {
            return;
        }
        for (Activity activity : activities) {
            if(activity instanceof WifiConnectActivity) {
                activity.finish();
            }
            if(activity instanceof AddWifiActivity) {
                activity.finish();
            }
        }
    }

    private void threeSecFinish() {
        // 3秒后销毁
        new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(this::finish), 3000);
    }

}
