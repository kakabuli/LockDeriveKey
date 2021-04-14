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
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ActivityUtils;
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
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.sign.LoginActivity;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.Constant.USER_MAIL;
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

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
            return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
            //指定为经典Footer，默认是 BallPulseFooter
            return new ClassicsFooter(context).setDrawableSize(20);
        });
    }

    private MailLoginBeanRsp.DataBean mUserBean;
    private static App instance;
    private final String mFilePath = File.pathSeparator + "Ble" + File.pathSeparator;
    private CacheDiskUtils mCacheDiskUtils;

    public static App getInstance() {
        return instance;
    }

    protected MqttService mqttService;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        initCacheDisk();
        initMqttService();
    }

    // TODO: 2021/3/8 临时存个MainActivity 后期删除
    private MainActivity mMainActivity;

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    private void initCacheDisk() {
        File file = new File(getDir("Ble", MODE_PRIVATE) + mFilePath);
        mCacheDiskUtils = CacheDiskUtils.getInstance(file);
    }

    private User mUser;
    private String mMail;
    private BleDeviceLocal mBleDeviceLocal;

    public BleDeviceLocal getBleDeviceLocal() {
        return mBleDeviceLocal;
    }

    public void setBleDeviceLocal(BleDeviceLocal bleDeviceLocal) {
        mBleDeviceLocal = bleDeviceLocal;
    }

    public User getUserFromLocal(String mail) {
        mMail = mail;
        SPUtils.getInstance(REVOLO_SP).put(USER_MAIL, mail);
        mUser = AppDatabase.getInstance(getApplicationContext()).userDao().findUserFromMail(mail);
        return mUser;
    }

    public User getUser() {
        if(mMail == null) {
            mMail = SPUtils.getInstance(REVOLO_SP).getString(USER_MAIL);
        }
        if(mUser == null) {
            return getUserFromLocal(mMail);
        }
        return mUser;
    }

    private final ArrayList<BleBean> mConnectedBleBeanList = new ArrayList<>();
    private static final int DEFAULT_CONNECTED_CAPACITY = 3;

    private void addConnectedBleBean(BleBean bleBean) {
        if(mConnectedBleBeanList.size() == DEFAULT_CONNECTED_CAPACITY) {
            Timber.d("连接的蓝牙设备数量： %1d", mConnectedBleBeanList.size());
            for (BleBean ble : mConnectedBleBeanList) {
                Timber.d("已连接的蓝牙设备mac：%1s, esn: %2s",
                        ble.getOKBLEDeviceImp().getMacAddress(), ble.getEsn());
            }
            BleBean willRemoveBleBean = mConnectedBleBeanList.get(0);
            willRemoveBleBean.getOKBLEDeviceImp().disConnect(false);
            mConnectedBleBeanList.remove(0);
        }
        mConnectedBleBeanList.add(bleBean);
    }

    public void removeConnectedBleBeanAndDisconnect(@NotNull BleBean bean) {
        if(mConnectedBleBeanList.isEmpty()) {
            return;
        }
        if(bean.getOKBLEDeviceImp() != null) {
            bean.getOKBLEDeviceImp().disConnect(false);
        }
        for (BleBean bleBean : mConnectedBleBeanList) {
            if(bleBean.getEsn().equals(bean.getEsn())) {
                mConnectedBleBeanList.remove(bleBean);
            }
        }
    }

    public BleBean getBleBeanFromMac(@NotNull String mac) {
        for (BleBean bleBean : mConnectedBleBeanList) {
            if(bleBean.getOKBLEDeviceImp().getMacAddress().equals(mac)) {
                return bleBean;
            }
        }
        return null;
    }

    public BleBean connectDevice(BLEScanResult bleScanResult,  byte[] pwd1, byte[] pwd2, OnBleDeviceListener onBleDeviceListener, boolean isAppPair) {
        OKBLEDeviceImp deviceImp = new OKBLEDeviceImp(getApplicationContext(), bleScanResult);
        BleBean bleBean = new BleBean(deviceImp);
        bleBean.setPwd1(pwd1);
        bleBean.setPwd2(pwd2);
        bleBean.setOnBleDeviceListener(onBleDeviceListener);
        bleBean.setAppPair(isAppPair);
        bleBean.setAuth(false);
        addConnectedBleBean(bleBean);
        OKBLEDeviceListener okbleDeviceListener = new OKBLEDeviceListener() {
            @Override
            public void onConnected(String deviceTAG) {
                Timber.d("onConnected deviceTAG: %1s", deviceTAG);
                openControlNotify(deviceImp);
                if(bleBean.isAppPair()) {
                    // 正在蓝牙本地配网，所以不走自动鉴权
                    bleConnectedCallback(bleBean, deviceImp.getMacAddress());
                    return;
                }
                // 连接后都走自动鉴权流程
                bleBean.setAuth(true);
                new Handler(Looper.getMainLooper()).postDelayed(() -> App.getInstance().writeControlMsg(BleCommandFactory
                                .authCommand(bleBean.getPwd1(), bleBean.getPwd2(), bleBean.getEsn().getBytes(StandardCharsets.UTF_8)),
                        deviceImp), 50);

                bleConnectedCallback(bleBean, deviceImp.getMacAddress());
            }

            @Override
            public void onDisconnected(String deviceTAG) {
                Timber.d("onDisconnected deviceTAG: %1s", deviceTAG);
                if(bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("onDisconnected bleBean.getOnBleDeviceListener() == null");
                    return;
                }
                bleBean.getOnBleDeviceListener().onDisconnected(deviceImp.getMacAddress());
            }

            @Override
            public void onReadBattery(String deviceTAG, int battery) {
            }

            @Override
            public void onReceivedValue(String deviceTAG, String uuid, byte[] value) {
                Timber.d("onReceivedValue value: %1s", ConvertUtils.bytes2HexString(value));
                int cmd = BleByteUtil.byteToInt(value[3]);
                if(cmd == CMD_ENCRYPT_KEY_UPLOAD) {
                    authProcess(value, bleBean, deviceImp.getMacAddress());
                }
                if(bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                bleBean.getOnBleDeviceListener().onReceivedValue(deviceImp.getMacAddress(), uuid, value);
            }

            @Override
            public void onWriteValue(String deviceTAG, String uuid, byte[] value, boolean success) {
                Timber.d("onWriteValue uuid: %1s, value: %2s, success: %3b",
                        uuid, ConvertUtils.bytes2HexString(value), success);
                if(bleBean.getOnBleDeviceListener() == null) {
                    Timber.e("mOnBleDeviceListener == null");
                    return;
                }
                bleBean.getOnBleDeviceListener().onWriteValue(deviceImp.getMacAddress(), uuid, value, success);

            }

            @Override
            public void onReadValue(String deviceTAG, String uuid, byte[] value, boolean success) {
            }

            @Override
            public void onNotifyOrIndicateComplete(String deviceTAG, String uuid, boolean enable, boolean success) {
            }
        };
        deviceImp.addDeviceListener(okbleDeviceListener);
        // 自动重连
        deviceImp.connect(true);
        return bleBean;
    }

    private void bleConnectedCallback(@NotNull BleBean bleBean, @NotNull String mac) {
        if (bleBean.getOnBleDeviceListener() == null) {
            Timber.e("mOnBleDeviceListener == null");
            return;
        }
        bleBean.getOnBleDeviceListener().onConnected(mac);
    }

    private void authProcess(byte[] value, @NotNull BleBean bleBean, @NotNull String mac) {
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd2Or3 = bleBean.isAuth()?bleBean.getPwd2():bleBean.getPwd3();
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
            if(bleBean.getOnBleDeviceListener() != null) {
                bleBean.getOnBleDeviceListener().onAuthSuc(mac);
            }
            // 获取pwd3
            getPwd3(BleCommandFactory.ackCommand(BleByteUtil.byteToInt(value[1]), (byte) 0x00, cmd), decryptPayload, bleBean);
            bleBean.setAuth(false);
            // 鉴权成功后，同步当前时间
            syNowTime(bleBean);
        }
    }

    private void getPwd3(byte[] bytes, byte[] decryptPayload, @NotNull BleBean bleBean) {
        byte[] pwd3 = new byte[4];
        System.arraycopy(decryptPayload, 1, pwd3, 0, pwd3.length);
        Timber.d("鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(pwd3));
        // 内存存储
        bleBean.setPwd3(pwd3);
        App.getInstance().writeControlMsg(bytes, bleBean.getOKBLEDeviceImp());
    }

    private void syNowTime(@NotNull BleBean bleBean) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
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

    public void writeControlMsg(byte[] bytes, OKBLEDeviceImp deviceImp) {
        if(deviceImp != null) {
            deviceImp.addWriteOperation(sControlWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    public void writePairMsg(byte[] bytes, OKBLEDeviceImp deviceImp) {
        if(deviceImp != null) {
            deviceImp.addWriteOperation(sPairWriteCharacteristicUUID, bytes, mWriteOperationListener);
        }
    }

    private void openControlNotify(OKBLEDeviceImp deviceImp) {
        if(deviceImp != null) {
            deviceImp.addNotifyOrIndicateOperation(sControlNotifyCharacteristicUUID,
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

    public void openPairNotify(OKBLEDeviceImp deviceImp) {
        if(deviceImp != null) {
            deviceImp.addNotifyOrIndicateOperation(sPairNotifyCharacteristicUUID,
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
        Activity activity = ActivityUtils.getTopActivity();
        if(activity != null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.putExtra(Constant.IS_SHOW_DIALOG, isShowDialog);
            activity.startActivity(intent);
            ActivityUtils.finishOtherActivities(LoginActivity.class);
            Timber.d("token过期   ");
        }

        // TODO: 2021/3/7 断开所有蓝牙连接
        //清除内存中缓存的数据
//        if(mDevice != null) {
//            // 如果之前存在设备，需要先断开之前的连接
//            mDevice.disConnect(false);
//        }

        //清除数据库数据

    }

    public void logout(boolean isShowDialog, Activity act) {
        // TODO: 2021/3/30 logout的数据操作
        act.startActivity(new Intent(act, LoginActivity.class));
        act.finish();
    }

    /**
     * 删除持久化数据
     */
    private void clearData() {
        //TODO:未实现Room 清除ORM数据库
        File file = new File(getDir("Ble", MODE_PRIVATE) + mFilePath);
        file.delete();

    }

    private boolean isWifiSettingNeedToCloseBle = false;

    public boolean isWifiSettingNeedToCloseBle() {
        return isWifiSettingNeedToCloseBle;
    }

    public void setWifiSettingNeedToCloseBle(boolean wifiSettingNeedToCloseBle) {
        isWifiSettingNeedToCloseBle = wifiSettingNeedToCloseBle;
    }



    private List<BleDeviceLocal> mBleDeviceLocals = new ArrayList<>();

    public List<BleDeviceLocal> getBleDeviceLocals() {
        return mBleDeviceLocals;
    }

    public void addBleDeviceLocal(BleDeviceLocal bleDeviceLocal) {
        if(mBleDeviceLocals.contains(bleDeviceLocal)) {
            return;
        }
        mBleDeviceLocals.add(bleDeviceLocal);
    }

    public void addBleDeviceLocals(List<BleDeviceLocal> bleDeviceLocals) {
        mBleDeviceLocals.clear();
        mBleDeviceLocals.addAll(bleDeviceLocals);
    }

    public void removeBleDeviceLocalFromMac(@NotNull String esn) {
        if(mBleDeviceLocals.isEmpty()) {
            return;
        }
        for (BleDeviceLocal ble : mBleDeviceLocals) {
            if(ble.getEsn().equals(esn)) {
                mBleDeviceLocals.remove(ble);
                return;
            }
        }
    }

    /*-------------------------------- 蓝牙搜索 --------------------------------*/
    private OKBLEScanManager mScanManager;

    public OKBLEScanManager getScanManager() {
        if(mScanManager == null) {
            mScanManager = new OKBLEScanManager(this);
        }
        return mScanManager;
    }

    public void setScanCallBack(DeviceScanCallBack scanCallBack) {
        mScanManager.setScanCallBack(scanCallBack);
        mScanManager.setScanDuration(20*1000);
    }

    /*-------------------------------- 地理围栏设备 ---------------------------------*/

    private BleDeviceLocal mUsingGeoFenceBleDeviceLocal;

    public BleDeviceLocal getUsingGeoFenceBleDeviceLocal() {
        return mUsingGeoFenceBleDeviceLocal;
    }

    public void setUsingGeoFenceBleDeviceLocal(BleDeviceLocal usingGeoFenceBleDeviceLocal) {
        mUsingGeoFenceBleDeviceLocal = usingGeoFenceBleDeviceLocal;
    }
}
