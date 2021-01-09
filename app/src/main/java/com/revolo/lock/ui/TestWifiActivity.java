package com.revolo.lock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.core.OKBLEDevice;
import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/7
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试wifi配网
 */
public class TestWifiActivity extends BaseActivity {

    private TextView mTvLog;
    private EditText mEtWifiSn;
    private EditText mEtWifiPwd;

    private final int mQRPre = 1;
    private final int mDefault = 0;
    private final int mESNPre = 2;
    private int mPreA = mDefault;
    private String mEsn;
    private String mMac;
    private String mSystemId;

    private OKBLEScanManager mScanManager;

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
        addLog(StringUtils.format("initData QR Code: %1s\n", qrResult));
        if(list.length == 3) {
            // 分成三份是正确的
            // ESN=S420210110001&MAC=10:98:C3:72:C6:23&SystemID=edbf0f0d029615ed
            mEsn = list[0].substring(4).trim();
            mMac = list[1].substring(4).trim();
            mSystemId = list[2].substring(9).trim();
            addLog(StringUtils.format("initData Mac: %1s\n", mMac));
        }
    }

    private void initDataFromEsnPre(Intent intent) {
        mPreA = mESNPre;
        if(!intent.hasExtra(Constant.ESN)) return;
        mEsn = intent.getStringExtra(Constant.ESN);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_test_wifi;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        mTvLog = findViewById(R.id.tvLog);
        mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        mEtWifiSn = findViewById(R.id.etWifiSn);
        mEtWifiPwd = findViewById(R.id.etWifiPwd);
        Button btnSend = findViewById(R.id.btnSend);
        applyDebouncingClickListener(btnSend);
    }

    @Override
    public void doBusiness() {
        if(mPreA == mQRPre || mPreA == mESNPre) {
            initScanManager();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnSend) {
            if(!isStartSend) {
                startSendWifiInfo();
            }
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
                addLog("该设备不支持BLE\n");
                break;
            case DeviceScanCallBack.SCAN_FAILED_BLUETOOTH_DISABLE:
                addLog("请打开手机蓝牙\n");
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE:
                addLog("请授予位置权限以扫描周围的蓝牙设备\n");
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE_FOREVER:
                addLog("位置权限被您永久拒绝,请在设置里授予位置权限以扫描周围的蓝牙设备\n");
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
            addLog("扫描到设备\n");
            connectDevice(device);
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
            addLog(StringUtils.format("device esn: %1s, input esn: %2s\n", sn, esn));
            return sn.equalsIgnoreCase(esn);
        }
        return false;
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<byte[]> mWifiSnDataList = new ArrayList<>();
    private int mWifiSnCount = 0;
    private int mWifiPwdCount = 0;
    private final List<byte[]> mWifiPwdDataList = new ArrayList<>();
    private int mWifiSnLen = 0;
    private int mWifiPwdLen = 0;
    private boolean isStartSend = false;

    private void startSendWifiInfo() {
        mWifiSnDataList.clear();
        mWifiPwdDataList.clear();
        mWifiSnCount = 0;
        mWifiPwdCount = 0;
        mWifiSnLen = 0;
        mWifiPwdLen = 0;
        isStartSend = true;
        String wifiSn = mEtWifiSn.getText().toString().trim();
        String wifiPwd = mEtWifiPwd.getText().toString().trim();
        if(TextUtils.isEmpty(wifiSn)) {
            ToastUtils.showShort("请输入WifiSn");
            return;
        }
        if(TextUtils.isEmpty(wifiPwd)) {
            ToastUtils.showShort("请输入wifi密码");
            return;
        }

        byte[] wifiSnBytes = wifiSn.getBytes(StandardCharsets.UTF_8);
        addLog(StringUtils.format("WifiSn: %1s\n", ConvertUtils.bytes2HexString(wifiSnBytes)));
        mWifiSnLen = wifiSnBytes.length;
        for (int i=0; i<mWifiSnLen; i=i+14) {
            int maxIndex = Math.min((mWifiSnLen - i), 14);
            byte[] data = new byte[maxIndex];
            System.arraycopy(wifiSnBytes, i, data, 0, data.length);
            Timber.d("mWifiSnLen data %1s, i: %2d", ConvertUtils.bytes2HexString(data), i);
            mWifiSnDataList.add(data);
        }

        byte[] wifiPwdBytes = wifiPwd.getBytes(StandardCharsets.UTF_8);
        addLog(StringUtils.format("WifiPwd: %1s\n", ConvertUtils.bytes2HexString(wifiPwdBytes)));
        mWifiPwdLen = wifiPwdBytes.length;
        for (int i=0; i<mWifiPwdLen; i=i+14) {
            int maxIndex = Math.min((mWifiPwdLen - i), 14);
            byte[] data = new byte[maxIndex];
            System.arraycopy(wifiPwdBytes, i, data, 0, data.length);
            Timber.d("mWifiPwdLen data %1s, i: %2d", ConvertUtils.bytes2HexString(data), i);
            mWifiPwdDataList.add(data);
        }

        writeWifiSn();

    }

    private final Runnable mWriteWifiSnRunnable = () -> {
        byte[] data = mWifiSnDataList.get(0);
        Timber.d("mWriteWifiSnRunnable data %1s", ConvertUtils.bytes2HexString(data));
        writeMsg(BleCommandFactory.sendSSIDCommand((byte) mWifiSnLen, (byte) mWifiSnCount, data));
        mWifiSnCount++;
        mWifiSnDataList.remove(0);
    };

    private final Runnable mWriteWifiPwdRunnable = () -> {
        byte[] data = mWifiPwdDataList.get(0);
        Timber.d("mWriteWifiPwdRunnable data %1s", ConvertUtils.bytes2HexString(data));
        writeMsg(BleCommandFactory.sendSSIDPwdCommand((byte) mWifiPwdLen, (byte) mWifiPwdCount, data));
        mWifiPwdCount++;
        mWifiPwdDataList.remove(0);
    };

    private void writeWifiSn() {
        if(mWifiSnDataList.isEmpty()) {
            if(isStartSend) {
                writeWifiPwd();
            }
            return;
        }
        mHandler.postDelayed(mWriteWifiSnRunnable, 50);
    }

    private void writeWifiPwd() {
        if(mWifiPwdDataList.isEmpty()) {
            isStartSend = false;
            return;
        }
        mHandler.postDelayed(mWriteWifiPwdRunnable, 50);
    }

    private OKBLEDevice mDevice;

    public void connectDevice(BLEScanResult bleScanResult) {
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }
        mDevice = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                addLog(StringUtils.format("%1s 蓝牙连接成功\n", mDevice.getBluetoothDevice().getAddress()));
                openPairNotify();
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                addLog("连接失败\n");
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                addLog(StringUtils.format("接收到的数据：%1s\n", ConvertUtils.bytes2HexString(value)));
                if(value == null) {
                    return;
                }
                if(value[3] == (byte)0x90) {
                    writeWifiSn();
                } else if(value[3] == (byte)0x91) {
                    writeWifiPwd();
                }
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
            }

            @Override
            public void onReadValue(String deviceTAG, String uuid, byte[] value, boolean success) {
            }

            @Override
            public void onNotifyOrIndicateComplete(String deviceTAG, String uuid, boolean enable, boolean success) {
            }
        };
        mDevice.addDeviceListener(okbleDeviceListener);
        // 自动重连
        mDevice.connect(true);

    }

    private static final String sPairWriteCharacteristicUUID = "FFC1";
    private static final String sPairNotifyCharacteristicUUID = "FFC6";

    private void openPairNotify() {
        if(mDevice != null) {
            mDevice.addNotifyOrIndicateOperation(sPairNotifyCharacteristicUUID,
                    true, new OKBLEOperation.NotifyOrIndicateOperationListener() {
                        @Override
                        public void onNotifyOrIndicateComplete() {
                            addLog("打开配网通知成功\n");
                        }

                        @Override
                        public void onFail(int code, String errMsg) {
                            addLog(StringUtils.format("打开配网失败 errMsg: %1s\n", errMsg));
                        }

                        @Override
                        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

                        }
                    });
        }
    }

    private void writeMsg(byte[] bytes) {
        if(mDevice != null) {
            mDevice.addWriteOperation(sPairWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    private final OKBLEOperation.WriteOperationListener mWriteOperationListener = new OKBLEOperation.WriteOperationListener() {
        @Override
        public void onWriteValue(byte[] value) {
            addLog(StringUtils.format("发送数据：%1s\n", ConvertUtils.bytes2HexString(value)));
        }

        @Override
        public void onFail(int code, String errMsg) {
            addLog(StringUtils.format("发送数据失败 errMsg: %1s\n", errMsg));
        }

        @Override
        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

        }
    };

    private void addLog(String msg) {
        if(mTvLog != null) {
            mTvLog.append(msg);
        }
    }

}
