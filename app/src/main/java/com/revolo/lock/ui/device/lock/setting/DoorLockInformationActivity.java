package com.revolo.lock.ui.device.lock.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门锁信息
 */
public class DoorLockInformationActivity extends BaseActivity {

    private TextView mTvLockSn, mTvWifiVersion, mTvFirmwareVersion;
    private View mVVersion;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_lock_infomation;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_lock_information));
        mVVersion = findViewById(R.id.vVersion);
        mTvLockSn = findViewById(R.id.tvLockSn);
        mTvWifiVersion = findViewById(R.id.tvWifiVersion);
        mTvFirmwareVersion = findViewById(R.id.tvFirmwareVersion);
    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initTestData() {
        mTvLockSn.setText("10V0204110001");
        mTvFirmwareVersion.setText("V1.0.0");
        mTvWifiVersion.setText("V1.0.0");
        mVVersion.setVisibility(View.VISIBLE);
    }

}
