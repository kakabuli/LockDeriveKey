package com.revolo.lock;

import android.app.Application;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.bean.BleBean;
import com.revolo.lock.ble.OnBleDeviceListener;

import java.io.File;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class App extends Application {

    private String mToken;
    private static App instance;
    private OKBLEDeviceImp mDevice;
    private BleBean mBleBean;
    private final String mFilePath = File.pathSeparator + "Ble" + File.pathSeparator;
    private CacheDiskUtils mCacheDiskUtils;

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
        initCacheDisk();
    }

    private void initCacheDisk() {
        File file = new File(getDir("Ble", MODE_PRIVATE) + mFilePath);
        mCacheDiskUtils = CacheDiskUtils.getInstance(file);
    }

    // TODO: 2021/1/28 后面监听需要修改 适应多设备
    private OnBleDeviceListener mOnBleDeviceListener;
    public void setOnBleDeviceListener(OnBleDeviceListener onBleDeviceListener) {
        mOnBleDeviceListener = onBleDeviceListener;
    }
    public void clearBleDeviceListener() {
        mOnBleDeviceListener = null;
    }

    public void connectDevice(BLEScanResult bleScanResult) {
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }
        mDevice = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        mBleBean = new BleBean(mDevice);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                Timber.d("onConnected deviceTAG: %1s", deviceTAG);
                openControlNotify();
                if(mOnBleDeviceListener != null) {
                    mOnBleDeviceListener.onConnected();
                }
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
                if(mOnBleDeviceListener != null) {
                    mOnBleDeviceListener.onDisconnected();
                }
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                if(mOnBleDeviceListener != null) {
                    mOnBleDeviceListener.onReceivedValue(uuid, value);
                }
                Timber.d("onReceivedValue value: %1s", ConvertUtils.bytes2HexString(value));
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                if(mOnBleDeviceListener != null) {
                    mOnBleDeviceListener.onWriteValue(uuid, value, success);
                }
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
    private final OKBLEOperation.WriteOperationListener mWriteOperationListener = new OKBLEOperation
            .WriteOperationListener() {
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

    public void writeControlMsg(byte[] bytes) {
        if(mDevice != null) {
            mDevice.addWriteOperation(sControlWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    public void writePairMsg(byte[] bytes) {
        if(mDevice != null) {
            mDevice.addWriteOperation(sPairWriteCharacteristicUUID, bytes, mWriteOperationListener);
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

    public void openPairNotify() {
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

    public BleBean getBleBean() {
        return mBleBean;
    }

    public CacheDiskUtils getCacheDiskUtils() {
        return mCacheDiskUtils;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }
}
