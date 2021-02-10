package com.revolo.lock;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.a1anwang.okble.client.core.OKBLEDeviceImp;
import com.a1anwang.okble.client.core.OKBLEDeviceListener;
import com.a1anwang.okble.client.core.OKBLEOperation;
import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.mqtt.MqttService;
import com.revolo.lock.ui.sign.LoginActivity;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD;
import static com.revolo.lock.ble.BleResultProcess.CONTROL_ENCRYPTION;
import static com.revolo.lock.ble.BleResultProcess.checksum;
import static com.revolo.lock.ble.BleResultProcess.pwdDecrypt;

/**
 * author :
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class App extends Application {

    private MailLoginBeanRsp.DataBean mUserBean;
    private static App instance;
    private OKBLEDeviceImp mDevice;
    private BleBean mBleBean;
    private final String mFilePath = File.pathSeparator + "Ble" + File.pathSeparator;
    private CacheDiskUtils mCacheDiskUtils;
    private final List<Activity> mWillFinishActivities = new ArrayList<>();

    public static App getInstance() {
        return instance;
    }

    protected MqttService mqttService;

    private boolean isUseBle = true;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        initCacheDisk();
        initMqttService();
        isUseBle = SPUtils.getInstance().getBoolean(Constant.IS_USE_BLE, true);
    }

    public boolean isUseBle() {
        return isUseBle;
    }

    public void setUseBle(boolean useBle) {
        isUseBle = useBle;
        SPUtils.getInstance().put(Constant.IS_USE_BLE, isUseBle);
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
        Timber.d("clearBleDeviceListener");
        mOnBleDeviceListener = null;
    }

    private boolean isAutoAuth = false;

    // 设置就进入自动鉴权
    public void setAutoAuth(boolean autoAuth) {
        isAutoAuth = autoAuth;
    }

    // TODO: 2021/2/8 存在bug, 后续要下沉自动鉴权的完整流程
    public void connectDevice(BLEScanResult bleScanResult) {
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }
        isAutoAuth = false;
        mDevice = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        mBleBean = new BleBean(mDevice);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                Timber.d("onConnected deviceTAG: %1s", deviceTAG);
                openControlNotify();
                if(isAutoAuth) {
                    // 走自动鉴权流程
                    App.getInstance().writeControlMsg(BleCommandFactory
                            .authCommand(mBleBean.getPwd1(), mBleBean.getPwd2(), mBleBean.getEsn().getBytes(StandardCharsets.UTF_8)));
                }
                if(mOnBleDeviceListener == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                mOnBleDeviceListener.onConnected();
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
                if(mOnBleDeviceListener == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                mOnBleDeviceListener.onDisconnected();
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                Timber.d("onReceivedValue value: %1s", ConvertUtils.bytes2HexString(value));
                if(isAutoAuth) {
                    int cmd = BleByteUtil.byteToInt(value[3]);
                    if(cmd == CMD_ENCRYPT_KEY_UPLOAD) {
                        authProcess(value, mBleBean.getPwd1(), mBleBean.getPwd3());
                    }
                }
                if(mOnBleDeviceListener == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                mOnBleDeviceListener.onReceivedValue(uuid, value);
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                Timber.d("onWriteValue uuid: %1s, value: %2s, success: %3b",
                        uuid, ConvertUtils.bytes2HexString(value), success);
                if(mOnBleDeviceListener == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                mOnBleDeviceListener.onWriteValue(uuid, value, success);

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

    private void authProcess(byte[] value, byte[] pwd1, byte[] pwd2Or3) {
        boolean isEncrypt = (value[0]==CONTROL_ENCRYPTION);
        byte[] payload = new byte[16];
        System.arraycopy(value,  4, payload, 0, payload.length);
        byte[] decryptPayload = isEncrypt?pwdDecrypt(payload, pwd1, pwd2Or3):payload;
        byte sum = checksum(decryptPayload);
        if(value[2] != sum) {
            Timber.d("authProcess 校验和失败，接收数据中的校验和：%1s，\n接收数据后计算的校验和：%2s",
                    ConvertUtils.int2HexString(value[2]), ConvertUtils.int2HexString(sum));
            return;
        }
        int cmd = BleByteUtil.byteToInt(value[3]);
        if(decryptPayload[0] == 0x02) {
            // 获取pwd3
            byte[] pwd3 = new byte[4];
            System.arraycopy(decryptPayload, 1, pwd3, 0, pwd3.length);
            Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(pwd3));
            // 内存存储
            mBleBean.setPwd3(pwd3);
            App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(BleByteUtil.byteToInt(value[1]), (byte)0x00, cmd));
            // 鉴权成功后，同步当前时间
            syNowTime();
        }
    }

    private void syNowTime() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, mBleBean.getPwd1(), mBleBean.getPwd3()));
        }, 20);
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

    public MailLoginBeanRsp.DataBean getUserBean() {
        return mUserBean;
    }

    public void setUserBean(MailLoginBeanRsp.DataBean userBean) {
        mUserBean = userBean;
    }

    /**
     * 启动MQTT服务
     */
    private void initMqttService() {
        Intent intent = new Intent(this, MqttService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof MqttService.MyBinder) {
                    MqttService.MyBinder binder = (MqttService.MyBinder) service;
                    mqttService = binder.getService();
                    Timber.d("attachView service启动" + (mqttService == null));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }


    public MqttService getMqttService() {
        return mqttService;
    }

    /**
     * @param isShowDialog 是否弹出对话框，提示用户token失效，需要重新登陆。如果是主动退出登录的那么不需要提示 是false
     *                     <p>
     *                     <p>
     *                     Token过期  需要处理的事情
     *                     清除token和UID
     *                     bleService  断开蓝牙连接  清除蓝牙数据
     *                     清除缓存到的本地数据
     *                     关闭所有界面，退出到登陆界面
     *                     ....
     */
    public void tokenInvalid(boolean isShowDialog) {
        clearData();  //清除数据库数据
        finishPreActivities();
        Timber.d("token过期   ");

        //清除内存中缓存的数据
        if(mDevice != null) {
            // 如果之前存在设备，需要先断开之前的连接
            mDevice.disConnect(false);
        }

        //清除数据库数据
        for (Activity activity : mWillFinishActivities) {
            if (activity != null) {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.putExtra(Constant.IS_SHOW_DIALOG, isShowDialog);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

    /**
     * 删除持久化数据
     */
    private void clearData() {
        //TODO:未实现Room 清除ORM数据库
        File file = new File(getDir("Ble", MODE_PRIVATE) + mFilePath);
        file.delete();

    }


    public void addWillFinishAct(Activity activity) {
        if(!mWillFinishActivities.contains(activity)) {
            mWillFinishActivities.add(activity);
        }

    }

    public List<Activity> getWillFinishActivities() {
        return mWillFinishActivities;
    }

    public void finishPreActivities() {
        if(mWillFinishActivities.isEmpty()) {
            return;
        }
        for (Activity activity : mWillFinishActivities) {
            if(activity != null) {
                activity.finish();
            }
        }
        mWillFinishActivities.clear();
    }

}
