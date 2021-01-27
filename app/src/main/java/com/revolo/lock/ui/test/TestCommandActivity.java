package com.revolo.lock.ui.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
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
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.bean.BleResultBean;

import java.nio.charset.StandardCharsets;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/11
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试下发指令页面
 */
public class TestCommandActivity extends BaseActivity {

    private TextView mTvLog;

    private String mEsn = "W010210110001";
    private String mMac = "4C:AA:16:C2:19:4E";

    private OKBLEScanManager mScanManager;
    private String mPwd1 = "534845B3EBF301BC1D0F688F00000000";

    @Override
    public void initData(@Nullable Bundle bundle) {
        addLog(StringUtils.format("initData Mac: %1s\n", mMac));
    }


    @Override
    public int bindLayout() {
        return R.layout.activity_test_command;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        mTvLog = findViewById(R.id.tvLog);
        mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        applyDebouncingClickListener(findViewById(R.id.btnSend));
    }

    @Override
    public void doBusiness() {
        initScanManager();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(isHavePwd2Or3) {
            if(view.getId() == R.id.btnSend) {
                String command = ((EditText) findViewById(R.id.etCommand)).getText().toString().trim();
                String cmd = ((EditText) findViewById(R.id.etCMD)).getText().toString().trim();
                if(command.length()%2 != 0 || command.length() != 32) {
                    addLog("输入payload数据位数不对\n");
                    return;
                }
                if(cmd.length() != 2) {
                    addLog("输入的CMD数据位数不对\n");
                    return;
                }
                @SuppressLint("UseSwitchCompatOrMaterialCode")
                Switch swEncrypt = findViewById(R.id.swEncrypt);
                boolean isEncrypt = swEncrypt.isChecked();
                byte[] send = ConvertUtils.hexString2Bytes(command);
                byte sendCmd = (byte) ConvertUtils.hexString2Int(cmd);
                writeMsg(BleCommandFactory
                        .commandPackage(isEncrypt, sendCmd, BleCommandFactory.commandTSN(),
                                send, ConvertUtils.hexString2Bytes(mPwd1),mPwd2Or3));
            }
        } else {
            addLog("鉴权没有成功，无法使用此功能\n");
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
        if(!TextUtils.isEmpty(device.getCompleteLocalName())&&device.getCompleteLocalName().contains("Rev")) {
            connectBleFromQRCode(device);
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

    private void connectBleFromQRCode(BLEScanResult device) {
        if(TextUtils.isEmpty(mMac)) return;
        if(device.getMacAddress().equalsIgnoreCase(mMac)) {
            mScanManager.stopScan();
            addLog("扫描到设备\n");
            connectDevice(device);
        }
    }

    private OKBLEDevice mDevice;
    private boolean isHavePwd2Or3 = false;
    private final byte[] mPwd2Or3 = new byte[4];
    
    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        auth(bleResultBean);
    };

    private void auth(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == 0x08) {
            byte[] data = bleResultBean.getPayload();
            if(data[0] == 0x01) {
                // 入网时
                // 获取pwd2
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
                isHavePwd2Or3 = true;
                writeMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    addLog(StringUtils.format("auth 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3)));
                    writeMsg(BleCommandFactory.authCommand(ConvertUtils.hexString2Bytes(mPwd1), mPwd2Or3, mEsn.getBytes(StandardCharsets.UTF_8)));
                }, 50);
            } else if(data[0] == 0x02) {
                // 获取pwd3
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
                addLog(StringUtils.format("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3)));
                writeMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
            }
        }
    }

    private void connectDevice(final BLEScanResult bleScanResult) {
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }
        mDevice = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                addLog(StringUtils.format("%1s 蓝牙连接成功\n", mDevice.getBluetoothDevice().getAddress()));
                openControlNotify();
                writeMsg(BleCommandFactory.pairCommand(ConvertUtils.hexString2Bytes(mPwd1), mEsn.getBytes(StandardCharsets.UTF_8)));
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
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value, ConvertUtils.hexString2Bytes(mPwd1), isHavePwd2Or3?mPwd2Or3:null, bleScanResult);
                
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

    private static final String sControlWriteCharacteristicUUID = "FFE9";
    private static final String sControlNotifyCharacteristicUUID = "FFE4";

    private void openControlNotify() {
        if(mDevice != null) {
            mDevice.addNotifyOrIndicateOperation(sControlNotifyCharacteristicUUID,
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
            mDevice.addWriteOperation(sControlWriteCharacteristicUUID, bytes, mWriteOperationListener);
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
