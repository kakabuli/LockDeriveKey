package com.revolo.lock.ui.home.add;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class InputESNActivity extends BaseActivity {

    private OKBLEScanManager  mScanManager;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_input_esn;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar("Manual input");
        setStatusBarColor(R.color.white);
        applyDebouncingClickListener(findViewById(R.id.btnNext));
    }

    @Override
    public void doBusiness() {
        mScanManager = new OKBLEScanManager(this);
        DeviceScanCallBack scanCallBack = new DeviceScanCallBack() {
            @Override
            public void onBLEDeviceScan(BLEScanResult device, int rssi) {
                if(!TextUtils.isEmpty(device.getCompleteLocalName())&&device.getCompleteLocalName().contains("KDS")) {
                    Timber.d(" scan: name:%1s  mac: %2s",
                            device.getCompleteLocalName(),  device.getMacAddress());
                }
            }

            @Override
            public void onFailed(int code) {
                switch (code){
                    case DeviceScanCallBack.SCAN_FAILED_BLE_NOT_SUPPORT:
                        Timber.e("该设备不支持BLE");
                        break;
                    case DeviceScanCallBack.SCAN_FAILED_BLUETOOTH_DISABLE:
                        Timber.e("请打开手机蓝牙");
                        break;
                    case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE:
                        Timber.e("请授予位置权限以扫描周围的蓝牙设备");
                        break;
                    case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE_FOREVER:
                        Timber.e("位置权限被您永久拒绝,请在设置里授予位置权限以扫描周围的蓝牙设备");
                        break;
                }
            }

            @Override
            public void onStartSuccess() {
            }
        };
        mScanManager.setScanCallBack(scanCallBack);
        mScanManager.startScan();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            // TODO: 2021/1/3 传递连接蓝牙
            Intent intent = new Intent(this, AddDeviceStep2BleConnectActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mScanManager != null && !mScanManager.isScanning()) {
            mScanManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mScanManager != null && mScanManager.isScanning()) {
            mScanManager.stopScan();
        }
    }

    @Override
    protected void onDestroy() {
        if(mScanManager != null && mScanManager.isScanning()) {
            mScanManager.stopScan();
        }
        super.onDestroy();
    }
}
