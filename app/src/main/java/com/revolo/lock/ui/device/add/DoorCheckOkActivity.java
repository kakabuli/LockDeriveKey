package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import timber.log.Timber;

public class DoorCheckOkActivity extends BaseActivity {

    private boolean isGoToAddWifi = true;
    private Button mBtnNext;
    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.IS_GO_TO_ADD_WIFI)) {
            isGoToAddWifi = intent.getBooleanExtra(Constant.IS_GO_TO_ADD_WIFI, true);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_check_ok_view;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.door_sensor_check_activity_title));
        mBtnNext = findViewById(R.id.btnNext);
        isGoToAddWifi = getIntent().getBooleanExtra("isGoToAddWifi", false);
        mBtnNext.setText(isGoToAddWifi ? getString(R.string.connect_wifi) : getString(R.string.complete));
        applyDebouncingClickListener(findViewById(R.id.btnNext), findViewById(R.id.tvSkip));
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
        if (view.getId() == R.id.btnNext) {
            if (isGoToAddWifi) {
                gotoAddWifi();
            } else {
                mBleDeviceLocal.setOpenDoorSensor(true);
                AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 50);
            }
        } else if (view.getId() == R.id.tvSkip) {
            if (isGoToAddWifi) {
                gotoAddWifi();
            } else {
                finish();
            }
        }
    }

    private void gotoAddWifi() {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        Timber.e("当前电量：" + mBleDeviceLocal.getLockPower());
        if (mBleDeviceLocal.getLockPower() <= 20) {
            // 低电量
            finish();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(DoorCheckOkActivity.this, AddWifiActivity.class);
                startActivity(intent);
                finish();
            }, 50);
        }
    }
}
