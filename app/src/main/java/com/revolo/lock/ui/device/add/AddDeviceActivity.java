package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加设备页面
 */
public class AddDeviceActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        applyDebouncingClickListener(findViewById(R.id.llAddLock));
    }

    @Override
    public void doBusiness() {
        App.getInstance().addWillFinishAct(this);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId()  == R.id.llAddLock) {
            startActivity(new Intent(this, AddDeviceStep1Activity.class));
        }
    }
}
