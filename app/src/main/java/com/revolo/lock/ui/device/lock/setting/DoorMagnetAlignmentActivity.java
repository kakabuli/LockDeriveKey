package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.device.add.DoorSensorCheckActivity;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 */
public class DoorMagnetAlignmentActivity extends BaseActivity {

    private ConstraintLayout mClAutoLock, mClTip;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_magnet_alignment;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_magnet_alignment));
        mClAutoLock = findViewById(R.id.clAutoLock);
        mClTip = findViewById(R.id.clTip);
        applyDebouncingClickListener(findViewById(R.id.btnNext), findViewById(R.id.ivDoorMagneticEnable));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            startActivity(new Intent(this, DoorSensorCheckActivity.class));
            finish();
            return;
        }
        if(view.getId() == R.id.ivDoorMagneticEnable) {
            mClAutoLock.setVisibility(View.GONE);
            mClTip.setVisibility(View.VISIBLE);
        }
    }
}
