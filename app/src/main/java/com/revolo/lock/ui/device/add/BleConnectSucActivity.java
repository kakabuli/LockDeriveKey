package com.revolo.lock.ui.device.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import java.util.List;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleConnectSucActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_ble_connect_suc;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        applyDebouncingClickListener(findViewById(R.id.btnAddWifi));
    }

    @Override
    public void doBusiness() {
        finishPreActivities();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnAddWifi) {
            startActivity(new Intent(this, DoorSensorCheckActivity.class));
            finish();
        }
    }

    private void finishPreActivities() {
        List<Activity> activities = ActivityUtils.getActivityList();
        if(activities.isEmpty()) {
            return;
        }
        for (Activity activity : activities) {
            if(activity instanceof AddDeviceStep1Activity) {
                activity.finish();
            }
            if(activity instanceof AddDeviceActivity) {
                activity.finish();
            }
        }
    }

}
