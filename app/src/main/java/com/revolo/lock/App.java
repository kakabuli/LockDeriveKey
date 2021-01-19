package com.revolo.lock;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.a1anwang.okble.client.core.OKBLEDevice;
import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.bean.BleResultBean;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class App extends Application {

    private static App instance;
    private OKBLEDevice mDevice;
    private boolean isHavePwd2Or3 = false;
    private final byte[] mPwd2Or3 = new byte[4];
    private byte[] mSystemId;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

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
                    Timber.d("auth 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3));
                    writeMsg(BleCommandFactory.authCommand(BleCommandFactory.sTestPwd1, mPwd2Or3, mSystemId));
                }, 50);
            } else if(data[0] == 0x02) {
                // 获取pwd3
                System.arraycopy(data, 1, mPwd2Or3, 0, mPwd2Or3.length);
                Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd2Or3));
                writeMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
            }
        }
    }

    public void connectDevice(BLEScanResult bleScanResult, byte[] systemId) {
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }
        mSystemId = systemId;
        mDevice = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                Timber.d("onConnected deviceTAG: %1s", deviceTAG);
                openControlNotify();
                writeMsg(BleCommandFactory.pairCommand(BleCommandFactory.sTestPwd1, systemId));
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                Timber.d("onReceivedValue value: %1s", ConvertUtils.bytes2HexString(value));
                if(value == null) {
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value, BleCommandFactory.sTestPwd1, isHavePwd2Or3?mPwd2Or3:null, bleScanResult);
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                Timber.d("onWriteValue uuid: %1s, value: %2s, success: %3b",
                        uuid, ConvertUtils.bytes2HexString(value), success);
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

    private static final String sPairWriteCharacteristicUUID = "FFC1";
    private static final String sPairNotifyCharacteristicUUID = "FFC6";
    private final OKBLEOperation.WriteOperationListener mWriteOperationListener = new OKBLEOperation.WriteOperationListener() {
        @Override
        public void onWriteValue(byte[] value) {
        }

        @Override
        public void onFail(int code, String errMsg) {
            Timber.e("onFail errMsg: %1s", errMsg);
        }

        @Override
        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

        }
    };

    private void writeMsg(byte[] bytes) {
        if(mDevice != null) {
            mDevice.addWriteOperation(sControlWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    private void openControlNotify() {
        if(mDevice != null) {
            mDevice.addNotifyOrIndicateOperation(sControlNotifyCharacteristicUUID,
                    true, new OKBLEOperation.NotifyOrIndicateOperationListener() {
                        @Override
                        public void onNotifyOrIndicateComplete() {
                            Timber.d("openControlNotify onNotifyOrIndicateComplete 打开控制命令通知成功");
                        }

                        @Override
                        public void onFail(int code, String errMsg) {
                            Timber.e("openControlNotify onFail errMsg: %1s", errMsg);
                        }

                        @Override
                        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

                        }
                    });
        }
    }

    private void openPairNotify() {
        if(mDevice != null) {
            mDevice.addNotifyOrIndicateOperation(sPairNotifyCharacteristicUUID,
                    true, new OKBLEOperation.NotifyOrIndicateOperationListener() {
                        @Override
                        public void onNotifyOrIndicateComplete() {
                            Timber.d("openControlNotify onNotifyOrIndicateComplete 打开配网通知成功");
                        }

                        @Override
                        public void onFail(int code, String errMsg) {
                            Timber.e("openControlNotify onFail errMsg: %1s", errMsg);
                        }

                        @Override
                        public void onExecuteSuccess(OKBLEOperation.OperationType type) {

                        }
                    });
        }
    }

}
