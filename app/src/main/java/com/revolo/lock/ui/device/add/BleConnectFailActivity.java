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
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class BleConnectFailActivity extends BaseActivity {

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_ble_connect_fail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.ble_connectsuc_activity_fail_title));
        applyDebouncingClickListener(findViewById(R.id.tvCancel), findViewById(R.id.btnReconnect));
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
        if(view.getId() == R.id.tvCancel) {
            gotoAddWifi();
            return;
        }
        if(view.getId() == R.id.btnReconnect) {
            reconnectBle();
        }
    }

    private void gotoAddWifi() {
        finish();
    }

    private void reconnectBle() {
        Intent intent = new Intent(this, AddDeviceStep2BleConnectActivity.class);
        Intent preIntent = getIntent();
        if(!preIntent.hasExtra(Constant.PRE_A)) return;
        String preA = preIntent.getStringExtra(Constant.PRE_A);
        intent.putExtra(Constant.PRE_A, preA);
        if(preA.equals(Constant.INPUT_ESN_A)) {
            intent.putExtra(Constant.ESN, preIntent.getStringExtra(Constant.ESN));
        } else if(preA.equals(Constant.QR_CODE_A)) {
            intent.putExtra(Constant.QR_RESULT, preIntent.getStringExtra(Constant.QR_RESULT));
        }
        startActivity(intent);
        finish();
    }

}
