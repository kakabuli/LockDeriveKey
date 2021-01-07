package com.revolo.lock.ui.home.add;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

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
    private String mSystemId;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.PRE_A)) return;
        String preA = intent.getStringExtra(Constant.PRE_A);
        if(preA.equals(Constant.INPUT_ESN_A)) {
            initDataFromEsnPre(intent);
        } else if(preA.equals(Constant.QR_CODE_A)) {
            initDataFromQRPre(intent);
        }
    }

    private void initDataFromQRPre(Intent intent) {
        mPreA = mQRPre;
        if(!intent.hasExtra(Constant.QR_RESULT)) return;
        String qrResult = intent.getStringExtra(Constant.QR_RESULT);
        if(TextUtils.isEmpty(qrResult)) return;
        String[] list = qrResult.split("&");
        Timber.d("initData QR Code: %1s", qrResult);
        if(list.length == 3) {
            // 分成三份是正确的
            // ESN=S420210110001&MAC=10:98:C3:72:C6:23&SystemID=edbf0f0d029615ed
            mEsn = list[0].substring(4).trim();
            mMac = list[1].substring(4).trim();
            mSystemId = list[2].substring(9).trim();
            Timber.d("initData Mac: %1s", mMac);
        }
    }

    private void initDataFromEsnPre(Intent intent) {
        mPreA = mESNPre;
        if(!intent.hasExtra(Constant.ESN)) return;
        mEsn = intent.getStringExtra(Constant.ESN);
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
        if(mPreA == mQRPre || mPreA == mESNPre) {
            initScanManager();
        } else {
            startActivity(new Intent(this, BleConnectFailActivity.class));
            finish();
        }
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
                filterAndConnectBle(device);
            }

            @Override
            public void onFailed(int code) {
                bleScanFailed(code);
            }

            @Override
            public void onStartSuccess() {
            }
        };
        mScanManager.setScanCallBack(scanCallBack);
        mScanManager.setScanDuration(20*1000);
        mScanManager.startScan();
    }

    private void filterAndConnectBle(BLEScanResult device) {
        if(!TextUtils.isEmpty(device.getCompleteLocalName())&&device.getCompleteLocalName().contains("KDS")) {
            Timber.d(" scan: name:%1s  mac: %2s",
                    device.getCompleteLocalName(),  device.getMacAddress());
            if(mPreA == mQRPre) {
                connectBleFromQRCode(device);
            } else if(mPreA == mESNPre) {
                connectBleFromInputEsn(device);
            }
        }
    }

    private void bleScanFailed(int code) {
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

    private void connectBleFromInputEsn(BLEScanResult device) {
        if(TextUtils.isEmpty(mEsn)) return;
        if(isDeviceEsnEqualsInputEsn(device, mEsn)) {
            mScanManager.stopScan();
            App.getInstance().connectDevice(device, ConvertUtils.hexString2Bytes(mSystemId));
        }
    }

    private void connectBleFromQRCode(BLEScanResult device) {
        if(TextUtils.isEmpty(mMac)) return;
        if(device.getMacAddress().equalsIgnoreCase(mMac)) {
            mScanManager.stopScan();
            Timber.d("connectBleFromQRCode 扫描到设备");
            App.getInstance().connectDevice(device, ConvertUtils.hexString2Bytes(mSystemId));
        }
    }

    private boolean isDeviceEsnEqualsInputEsn(BLEScanResult device, String esn) {
        //返回Manufacture ID之后的data
        SparseArray<byte[]> hex16 = device.getManufacturerSpecificData();
        if(hex16 != null && hex16.size() > 0) {
            StringBuilder sb = new StringBuilder();
            byte[] value = hex16.valueAt(0);
            //过滤无用蓝牙广播数据
            if (value.length < 16) return false;
            //截取出SN
            for (int j = 3; j < 16; j++) {
                sb.append((char) value[j]);
            }
            String sn = sb.toString().trim();
            Timber.d("getAndCheckESN device esn: %1s, input esn: %2s", sn, esn);
            return sn.equalsIgnoreCase(esn);
        }
        return false;
    }

}
