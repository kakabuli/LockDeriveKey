package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备设置
 */
public class DeviceSettingActivity extends BaseActivity {

    private TextView mTvName, mTvWifiName;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_setting));
        mTvName = findViewById(R.id.tvName);
        mTvWifiName = findViewById(R.id.tvWifiName);
        applyDebouncingClickListener(mTvName, mTvWifiName,
                findViewById(R.id.clAutoLock), findViewById(R.id.clPrivateMode),
                findViewById(R.id.clDuressCode), findViewById(R.id.clDoorLockInformation));
    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvName) {
            startActivity(new Intent(this, ChangeLockNameActivity.class));
            return;
        }
        if(view.getId() == R.id.tvWifiName) {
            startActivity(new Intent(this, WifiSettingActivity.class));
            return;
        }
        if(view.getId() == R.id.clAutoLock) {
            startActivity(new Intent(this, AutoLockActivity.class));
            return;
        }
        if(view.getId() == R.id.clPrivateMode) {
            startActivity(new Intent(this, PrivateModeActivity.class));
            return;
        }
        if(view.getId() == R.id.clDuressCode) {
            startActivity(new Intent(this, DuressCodeActivity.class));
            return;
        }
        if(view.getId() == R.id.clDoorLockInformation) {
            startActivity(new Intent(this, DoorLockInformationActivity.class));
        }
    }

    @Override
    public Resources getResources() {
        // 更改布局适应
        return AdaptScreenUtils.adaptHeight(super.getResources(), 703);
    }

    private void initTestData() {
        mTvName.setText("Tester");
        mTvWifiName.setText("Kaadas123");
    }

}
