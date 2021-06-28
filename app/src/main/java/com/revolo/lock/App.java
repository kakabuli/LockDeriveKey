package com.revolo.lock;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.manager.LockAppService;
import com.revolo.lock.manager.mqtt.MQTTManager;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.sign.LoginActivity;
import com.revolo.lock.ui.view.SmartClassicsFooterView;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.Constant.USER_MAIL;

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
            return new SmartClassicsHeaderView(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
            //指定为经典Footer，默认是 BallPulseFooter
            return new SmartClassicsFooterView(context).setDrawableSize(20);
        });
    }

    private MailLoginBeanRsp.DataBean mUserBean;
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    /*protected MqttService mMQttService;*/
    private LockAppService lockAppService;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        //initMQttService();
        initLockAppService();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
        });

        CrashCollectHandler.getInstance().init(this);

        CrashReport.initCrashReport(getApplicationContext(), "22dc9fa410", true);
    }

    // TODO: 2021/3/8 临时存个MainActivity 后期删除
    private MainActivity mMainActivity;

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    private User mUser;
    private String mMail;
    private BleDeviceLocal mBleDeviceLocal;
    private String mCurrSn;//当前设备
    private String mCurrMac;

    public BleDeviceLocal getBleDeviceLocal() {
        return mBleDeviceLocal;
    }

    public void setBleDeviceLocal(BleDeviceLocal bleDeviceLocal) {
        mBleDeviceLocal = bleDeviceLocal;
    }

    public String getmCurrSn() {
        return mCurrSn;
    }

    public void setmCurrSn(String mCurrSn) {
        this.mCurrSn = mCurrSn;
    }

    public String getmCurrMac() {
        return mCurrMac;
    }

    public void setmCurrMac(String mCurrMac) {
        this.mCurrMac = mCurrMac;
    }

    public User getUserFromLocal(String mail) {
        mMail = mail;
        SPUtils.getInstance(REVOLO_SP).put(USER_MAIL, mail);
        mUser = AppDatabase.getInstance(getApplicationContext()).userDao().findUserFromMail(mail);
        return mUser;
    }

    public User getUser() {
        if (mMail == null) {
            mMail = SPUtils.getInstance(REVOLO_SP).getString(USER_MAIL);
        }
        if (mUser == null) {
            return getUserFromLocal(mMail);
        }
        return mUser;
    }

    public MailLoginBeanRsp.DataBean getUserBean() {
        return mUserBean;
    }

    public void setUserBean(MailLoginBeanRsp.DataBean userBean) {
        mUserBean = userBean;
    }

    /**
     * 初始化LockAppService
     */
    private void initLockAppService() {
        Intent intent = new Intent(this, LockAppService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof LockAppService.lockBinder) {
                    LockAppService.lockBinder binder = (LockAppService.lockBinder) service;
                    lockAppService = binder.getService();
                    Timber.d("attachView service启动 %1b", (lockAppService == null));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                lockAppService = null;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public LockAppService getLockAppService() {
        return lockAppService;
    }

    /**
     * 获取用户设备列表
     *
     * @return
     */
    public List<BleDeviceLocal> getDeviceLists() {
        if (null != lockAppService) {
            return lockAppService.getUserDeviceList();
        }
        return new ArrayList<>();
    }

    public void removeDeviceList() {
        if (null != lockAppService) {
            lockAppService.removeDeviceList();
        }
    }

    /**
     * 设置用户列表
     *
     * @param bleDeviceLocals
     */
    public void setDeviceLists(List<BleDeviceLocal> bleDeviceLocals) {
        if (null != lockAppService) {
            lockAppService.add(bleDeviceLocals);
        }
    }

    /**
     * 获取用户设备
     *
     * @return
     */
    public BleBean getUserBleBean(String mac) {
        return lockAppService.getUserBleBean(mac);
    }

    /**
     * 删除蓝牙设备
     *
     * @param bean
     */
    public void removeConnectedBleDisconnect(@NotNull BleBean bean) {
        if (null != lockAppService) {
            if (null != bean.getOKBLEDeviceImp()) {
                lockAppService.removeBleConnect(bean.getOKBLEDeviceImp().getMacAddress());
            }
        }
    }

    public void removeConnectedBleDisconnect(String mac) {
        if (null != lockAppService) {
            if (null != mac && !"".equals(mac)) {
                lockAppService.removeDevice(mac, mac);
                lockAppService.removeBleConnect(mac);
            }
        }
    }
   /* public MqttService getMQttService() {
        return mMQttService;
    }*/

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
        Activity activity = ActivityUtils.getTopActivity();
        if (activity != null) {
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
        removeDeviceList();
        User user = App.getInstance().getUser();
        AppDatabase.getInstance(getApplicationContext()).userDao().delete(user);
        App.getInstance().getUserBean().setToken(""); // 清空token
        MQTTManager.getInstance().mqttDisconnect(); // mqtt断开连接
        SPUtils.getInstance(REVOLO_SP).put(Constant.USER_LOGIN_INFO, ""); // 清空登录信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                // 不要删除用户数据，你只做更新
                SPUtils.getInstance(REVOLO_SP).put(Constant.USER_LOGIN_INFO, ""); // 清空登录信息
            }
        }.start();
        act.startActivity(new Intent(act, LoginActivity.class).putExtra("logout", true));
    }

    private boolean isWifiSettingNeedToCloseBle = false;

    public boolean isWifiSettingNeedToCloseBle() {
        return isWifiSettingNeedToCloseBle;
    }

    public void setWifiSettingNeedToCloseBle(boolean wifiSettingNeedToCloseBle) {
        isWifiSettingNeedToCloseBle = wifiSettingNeedToCloseBle;
    }



    /*-------------------------------- 蓝牙搜索 --------------------------------*/
    private OKBLEScanManager mScanManager;

    public OKBLEScanManager getScanManager() {
        if (mScanManager == null) {
            mScanManager = new OKBLEScanManager(this);
        }
        return mScanManager;
    }

    public void setScanCallBack(DeviceScanCallBack scanCallBack) {
        mScanManager.setScanCallBack(scanCallBack);
        mScanManager.setScanDuration(20 * 1000);
    }

    public interface OnScanAndConnectResultListener {
        void connectResult(BleBean bleBean, BLEScanResult bleScanResult);
    }

    /*-------------------------------- 地理围栏设备 ---------------------------------*/

   /* private BleDeviceLocal mUsingGeoFenceBleDeviceLocal;

    public BleDeviceLocal getUsingGeoFenceBleDeviceLocal() {
        return mUsingGeoFenceBleDeviceLocal;
    }

    public void setUsingGeoFenceBleDeviceLocal(BleDeviceLocal usingGeoFenceBleDeviceLocal) {
        mUsingGeoFenceBleDeviceLocal = usingGeoFenceBleDeviceLocal;
    }
*/

    /*--------------------------------- 地理围栏功能 --------------------------------*/

    // TODO: 2021/4/20 现在的写法是存在问题，因为是基于一个设备来考虑的，如果出现多设备，这方案后续是需要修改的。
    // TODO: 2021/5/13 需要全局监听地理位置来实现地理围栏功能，国内google获取的坐标是火星坐标，存在很大偏差（但设备是海外的，所以最好直接在海外测试）
  /* private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Disposable mApproachOpenDisposable;
    private BleBean mGeoFenceBleBean;

    public void publishApproachOpen(String wifiID, int broadcastTime) {
        BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
        if (deviceLocal == null) {
            Timber.e("publishApproachOpen deviceLocal == null");
            return;
        }
        if (mMQttService == null) {
            Timber.e("publishApproachOpen mMQttService == null");
            return;
        }
        toDisposable(mApproachOpenDisposable);
        App.getInstance().setUsingGeoFenceBleDeviceLocal(deviceLocal);
        mApproachOpenDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.approachOpen(wifiID, broadcastTime,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd2()))))
                    .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.APP_ROACH_OPEN))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mApproachOpenDisposable);
                    processApproachOpen(mqttData);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    Timber.e(e);
                });
        mCompositeDisposable.add(mApproachOpenDisposable);
    }
*/
   /* private void processApproachOpen(MqttData mqttData) {
        if (TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishApproachOpen mqttData.getFunc() is empty");
            return;
        }
        if (mqttData.getFunc().equals(MQttConstant.APP_ROACH_OPEN)) {
            Timber.d("publishApproachOpen 无感开门: %1s", mqttData);
            WifiLockApproachOpenResponseBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockApproachOpenResponseBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }
            if (bean == null) {
                Timber.e("publishApproachOpen bean == null");
                return;
            }
            if (bean.getParams() == null) {
                Timber.e("publishApproachOpen bean.getParams() == null");
                return;
            }
            if (bean.getCode() != 200) {
                Timber.e("publishApproachOpen code : %1d", bean.getCode());
                return;
            }
            // TODO: 2021/3/5 开启成功，然后开启蓝牙并不断搜索设备
            connectBle();
        }
        Timber.d("publishApproachOpen %1s", mqttData.toString());
    }*/

    /*private boolean isRestartConnectingBle = false;

    private void connectBle() {
        if (mUsingGeoFenceBleDeviceLocal == null) {
            return;
        }
        isRestartConnectingBle = true;
        final OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
            @Override
            public void onConnected(@NotNull String mac) {

            }

            @Override
            public void onDisconnected(@NotNull String mac) {

            }

            @Override
            public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                if (value == null) {
                    Timber.e("mOnBleDeviceListener value == null");
                    return;
                }
                if (!mUsingGeoFenceBleDeviceLocal.getMac().equals(mac)) {
                    Timber.e("mOnBleDeviceListener mac: %1s, localMac: %2s", mac, mUsingGeoFenceBleDeviceLocal.getMac());
                    return;
                }
                BleBean bleBean = App.getInstance().getBleBeanFromMac(mUsingGeoFenceBleDeviceLocal.getMac());
                if (bleBean == null) {
                    Timber.e("mOnBleDeviceListener bleBean == null");
                    return;
                }
                if (bleBean.getOKBLEDeviceImp() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getOKBLEDeviceImp() == null");
                    return;
                }
                if (bleBean.getPwd1() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getPwd1() == null");
                    return;
                }
                if (bleBean.getPwd3() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getPwd3() == null");
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value, bleBean.getPwd1(), bleBean.getPwd3(),
                        bleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc(@NotNull String mac) {
                // 配对成功
                if (mac.equals(mUsingGeoFenceBleDeviceLocal.getMac())) {
                    isRestartConnectingBle = false;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        BleBean bleBean = App.getInstance().getBleBeanFromMac(mUsingGeoFenceBleDeviceLocal.getMac());
                        if (bleBean == null) {
                            Timber.e("mOnBleDeviceListener bleBean == null");
                            return;
                        }
                        // TODO: 2021/4/7 抽离0x01
                        App.getInstance().writeControlMsg(BleCommandFactory
                                .setKnockDoorAndUnlockTime(0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
                    }, 200);
                }
            }

        };
        mGeoFenceBleBean = App.getInstance().getBleBeanFromMac(mUsingGeoFenceBleDeviceLocal.getMac());
        if (mGeoFenceBleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mUsingGeoFenceBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if (bleScanResult != null) {
                mGeoFenceBleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mUsingGeoFenceBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mUsingGeoFenceBleDeviceLocal.getPwd2()),
                        onBleDeviceListener, false);
                mGeoFenceBleBean.setEsn(mUsingGeoFenceBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if (mGeoFenceBleBean.getOKBLEDeviceImp() != null) {
                mGeoFenceBleBean.setOnBleDeviceListener(onBleDeviceListener);
                if (!mGeoFenceBleBean.getOKBLEDeviceImp().isConnected()) {
                    mGeoFenceBleBean.getOKBLEDeviceImp().connect(true);
                }
                mGeoFenceBleBean.setPwd1(ConvertUtils.hexString2Bytes(mUsingGeoFenceBleDeviceLocal.getPwd1()));
                mGeoFenceBleBean.setPwd2(ConvertUtils.hexString2Bytes(mUsingGeoFenceBleDeviceLocal.getPwd2()));
                mGeoFenceBleBean.setEsn(mUsingGeoFenceBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
        // 1分钟后判断设备是否连接成功，否就恢复wifi状态，每秒判断一次是否配对设备成功
        mCountDownTimer.start();
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if (bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == BleProtocolState.CMD_KNOCK_DOOR_AND_UNLOCK_TIME) {
            if (bean.getPayload()[0] == 0) {
                // 设置敲击开锁成功
                if (mGeoFenceBleBean != null && mGeoFenceBleBean.getOKBLEDeviceImp() != null) {
                    mGeoFenceBleBean.getOKBLEDeviceImp().disConnect(false);
                }
            }
        }
    }


    private final CountDownTimer mCountDownTimer = new CountDownTimer(600000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mGeoFenceBleBean != null) {
                if (!isRestartConnectingBle) {
                    mCountDownTimer.cancel();
                }
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            if (mGeoFenceBleBean != null && mGeoFenceBleBean.getOKBLEDeviceImp() != null) {
                mGeoFenceBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }
    };

    private void toDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }*/

}
