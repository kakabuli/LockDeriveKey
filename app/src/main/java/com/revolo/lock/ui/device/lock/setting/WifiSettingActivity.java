package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.AddWifiActivity;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : wifi设置
 */
public class WifiSettingActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private ConstraintLayout clTip;
    private ImageView ivWifiEnable;
    private boolean isWifiConnected = false;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.BLE_DEVICE)) {
            // TODO: 2021/2/22 处理
            finish();
            return;
        }
        mBleDeviceLocal = intent.getParcelableExtra(Constant.BLE_DEVICE);
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_wifi_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_wifi_setting));
        clTip = findViewById(R.id.clTip);
        ivWifiEnable = findViewById(R.id.ivWifiEnable);

        updateUI();
        applyDebouncingClickListener(ivWifiEnable);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivWifiEnable) {
            if(isWifiConnected) {
                // TODO: 2021/2/26 执行关闭wifi
            } else {
                // TODO: 2021/2/26 跳转到连接wifi页面
//                Intent intent = new Intent(this, AddWifiActivity.class);
//                intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
//                startActivity(intent);
            }
        }
    }


    private void updateUI() {
        if(mBleDeviceLocal.getConnectedType() == 1) {
            // Wifi
            updateWifiState();
        } else if(mBleDeviceLocal.getConnectedType() == 2) {
            // 蓝牙
            updateBleState();
        } else {
            // TODO: 2021/2/26 do something
        }
    }

    private void updateWifiState() {
        ivWifiEnable.setImageResource(R.drawable.ic_icon_switch_open);
        clTip.setVisibility(View.GONE);
        isWifiConnected = true;
    }

    private void updateBleState() {
        ivWifiEnable.setImageResource(R.drawable.ic_icon_switch_close);
        clTip.setVisibility(View.VISIBLE);
        isWifiConnected = false;
    }
}
