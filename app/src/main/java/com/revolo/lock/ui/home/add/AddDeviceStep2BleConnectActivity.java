package com.revolo.lock.ui.home.add;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.core.OKBLEDevice;
import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 连接蓝牙或者搜寻到对应的蓝牙连接
 */
public class AddDeviceStep2BleConnectActivity extends BaseActivity {

    private OKBLEScanManager mScanManager;

    private final int mQRPre = 1;
    private final int mDefault = 0;
    private final int mESNPre = 2;
    private int mPreA = mDefault;
    private String mEsn;
    private String mMac;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.PRE_A)) return;
        String preA = intent.getStringExtra(Constant.PRE_A);
        if(preA.equals(Constant.INPUT_ESN_A)) {
            mPreA = mESNPre;
            if(!intent.hasExtra(Constant.ESN)) return;
            mEsn = intent.getStringExtra(Constant.ESN);
        } else if(preA.equals(Constant.QR_CODE_A)) {
            mPreA = mQRPre;
            if(!intent.hasExtra(Constant.QR_RESULT)) return;
            String qrResult = intent.getStringExtra(Constant.QR_RESULT);
            if(TextUtils.isEmpty(qrResult)) return;
            String[] list = qrResult.split("&");
            Timber.d("initData QR Code: %1s", qrResult);
            if(list.length == 3) {
                // 分成三份是正确的
                // ESN=S420210110001&MAC=10:98:C3:72:C6:23&SystemID=edbf0f0d029615ed
                mMac = list[1].substring(4).trim();
                Timber.d("initData Mac: %1s", mMac);
            }
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_step2_ble_connect;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        setStatusBarColor(R.color.white);

    }

    @Override
    public void doBusiness() {
        initScanManager();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

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

    private void initScanManager() {
        mScanManager = new OKBLEScanManager(this);
        DeviceScanCallBack scanCallBack = new DeviceScanCallBack() {
            @Override
            public void onBLEDeviceScan(BLEScanResult device, int rssi) {
                if(!TextUtils.isEmpty(device.getCompleteLocalName())&&device.getCompleteLocalName().contains("KDS")) {
                    Timber.d(" scan: name:%1s  mac: %2s",
                            device.getCompleteLocalName(),  device.getMacAddress());
                    if(mPreA == mQRPre) {
                        if(TextUtils.isEmpty(mMac)) return;
                        if(device.getMacAddress().equalsIgnoreCase(mMac)) {
                            mScanManager.stopScan();
                            Timber.d("扫描到设备");
//                            connectDevice(device);
                        }
                    }
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

    private void connectDevice(BLEScanResult bleScanResult) {
        // TODO: 2021/1/4 可能会内存泄漏，后面再看看怎么改正
        OKBLEDevice device = new OKBLEDeviceImp(mActivity, bleScanResult);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                Timber.d("onConnected deviceTAG: %1s", deviceTAG);
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
                Timber.d("onReadBattery battery: %1d", battery);
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                Timber.d("onReceivedValue uuid: %1s", uuid);
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                Timber.d("onWriteValue uuid: %1s value: %2s success: %3b",
                        uuid, ConvertUtils.bytes2HexString(value), success);
            }

            @Override
            public void onReadValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                Timber.d("onReadValue uuid: %1s value: %2s success: %3b",
                        uuid, ConvertUtils.bytes2HexString(value), success);
            }

            @Override
            public void onNotifyOrIndicateComplete(String deviceTAG, String uuid, boolean enable, boolean success) {
                Timber.d("onNotifyOrIndicateComplete uuid: %1s\n enable: %2b\n success: %3b",
                        uuid, enable, success);
            }
        };
        device.addDeviceListener(okbleDeviceListener);
        // 自动重连
        device.connect(true);
    }

}
