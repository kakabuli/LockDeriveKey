package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验失败
 */
public class DoorCheckFailActivity extends BaseActivity {

    private boolean isGoToAddWifi = true;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.IS_GO_TO_ADD_WIFI)) {
            isGoToAddWifi = intent.getBooleanExtra(Constant.IS_GO_TO_ADD_WIFI, true);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_check_fail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.door_sensor_check_activity_title));
        applyDebouncingClickListener(findViewById(R.id.btnTryAgain), findViewById(R.id.btnCancel));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnCancel) {
            if (isGoToAddWifi) {
                Intent intent = new Intent(this, AddWifiActivity.class);
                intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, isGoToAddWifi);
                startActivity(intent);
            }
            finish();
            return;
        }
        if (view.getId() == R.id.btnTryAgain) {
            Intent intent = new Intent(this, DoorSensorCheckActivity.class);
            intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, isGoToAddWifi);
            startActivity(intent);
            finish();
        }
    }
}
