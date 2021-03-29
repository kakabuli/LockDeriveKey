package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验失败
 */
public class DoorCheckFailActivity extends BaseActivity {

    @Override
    public void initData(@Nullable Bundle bundle) {
      
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_check_fail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_magnet_alignment));
        applyDebouncingClickListener(findViewById(R.id.btnTryAgain), findViewById(R.id.btnCancel));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnCancel) {
            startActivity(new Intent(this, DoorSensorCheckActivity.class));
            finish();
            return;
        }
        if(view.getId() == R.id.btnTryAgain) {
            Intent intent = new Intent(this, DoorSensorCheckActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
