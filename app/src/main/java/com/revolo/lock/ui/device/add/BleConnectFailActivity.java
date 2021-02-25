package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleConnectFailActivity extends BaseActivity {

    private long mDeviceId = -1L;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.DEVICE_ID)) {
            mDeviceId = intent.getLongExtra(Constant.DEVICE_ID, -1L);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_ble_connect_fail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        applyDebouncingClickListener(findViewById(R.id.tvCancel), findViewById(R.id.btnReconnect));
    }

    @Override
    public void doBusiness() {
        if(App.getInstance().getBleBean() != null
                && App.getInstance().getBleBean().getOKBLEDeviceImp() != null) {
            App.getInstance().getBleBean().getOKBLEDeviceImp().disConnect(false);
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvCancel) {
            gotoAddWifi();
            return;
        }
        if(view.getId() == R.id.btnReconnect) {
            reconnectBle();
        }
    }

    private void gotoAddWifi() {
        Intent intent = new Intent(this, AddWifiActivity.class);
        intent.putExtra(Constant.DEVICE_ID, mDeviceId);
        startActivity(intent);
        finish();
    }

    private void reconnectBle() {
        Intent intent = new Intent(this, AddDeviceStep2BleConnectActivity.class);
        Intent preIntent = getIntent();
        if(!preIntent.hasExtra(Constant.PRE_A)) return;
        String preA = preIntent.getStringExtra(Constant.PRE_A);
        intent.putExtra(Constant.PRE_A, preA);
        if(preA.equals(Constant.INPUT_ESN_A)) {
            preIntent.putExtra(Constant.ESN, intent.getStringExtra(Constant.ESN));
        } else if(preA.equals(Constant.QR_CODE_A)) {
            preIntent.putExtra(Constant.QR_RESULT, intent.getStringExtra(Constant.QR_RESULT));
        }
        startActivity(intent);
        finish();
    }

}
