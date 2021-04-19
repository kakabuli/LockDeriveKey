package com.revolo.lock.ui.device.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.revolo.lock.Constant;
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
            Intent intent = new Intent(this, DoorSensorCheckActivity.class);
            intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, true);
            startActivity(intent);
            finish();
        }
    }

    private void finishPreActivities() {
        ActivityUtils.finishActivity(AddDeviceStep1Activity.class);
        ActivityUtils.finishActivity(AddDeviceActivity.class);
        ActivityUtils.finishActivity(InputESNActivity.class);
    }

}
