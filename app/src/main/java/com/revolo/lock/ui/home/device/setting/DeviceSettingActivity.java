package com.revolo.lock.ui.home.device.setting;

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

    private TextView mTvName;

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
        applyDebouncingClickListener(mTvName);
    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvName) {
            startActivity(new Intent(this, ChangeLockNameActivity.class));
        }
    }

    @Override
    public Resources getResources() {
        // 更改布局适应
        return AdaptScreenUtils.adaptHeight(super.getResources(), 703);
    }

    private void initTestData() {
        mTvName.setText("Tester");
    }

}
