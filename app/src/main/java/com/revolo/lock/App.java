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
import com.revolo.lock.bean.request.DeleteDeviceTokenBeanReq;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.manager.LockAppService;
import com.revolo.lock.manager.geo.LockGeoFenceService;
import com.revolo.lock.manager.mqtt.MQTTManager;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.LockRecord;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;
import com.revolo.lock.ui.view.SmartClassicsFooterView;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
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
    private Map<String, List<LockRecord>> lockRecords = new HashMap<>();

    public void addLockRecords(String esn, List<LockRecord> records) {
        lockRecords.put(esn, records);
    }

    public List<LockRecord> getLockRecords(String esn) {
        return lockRecords.get(esn);
    }

    public void removeRecords(String esn) {
        if (null == esn || "".equals(esn)) {
            lockRecords.clear();
            return;
        }
        lockRecords.remove(esn);
    }

    public static App getInstance() {
        return instance;
    }

    /*protected MqttService mMQttService;*/
    private LockAppService lockAppService;
    private LockGeoFenceService lockGeoFenceService;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Timber.plant(new Timber.DebugTree());
        initLockAppService();
        initLockGeoService();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
        });

        CrashCollectHandler.getInstance().init(this);

        CrashReport.initCrashReport(getApplicationContext(), "22dc9fa410", true);
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
     * 初始化定位服务
     */
    private void initLockGeoService() {
        Intent intent = new Intent(this, LockGeoFenceService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof LockGeoFenceService.lockBinder) {
                    LockGeoFenceService.lockBinder binder = (LockGeoFenceService.lockBinder) service;
                    lockGeoFenceService = binder.getService();
                    Timber.d("attachView service启动 %1b", (lockGeoFenceService == null));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                lockGeoFenceService = null;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public LockGeoFenceService getLockGeoFenceService() {
        return lockGeoFenceService;
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
     * 绑定成功后，添加在设备列表中
     * @param bleDeviceLocal
     */
    public void addBleDeviceLocal(BleDeviceLocal bleDeviceLocal) {
        if (null != lockAppService) {
            List<BleDeviceLocal> bleDeviceLocalList = new ArrayList<>();
            bleDeviceLocalList.add(bleDeviceLocal);
            lockAppService.add(bleDeviceLocalList);
        }
    }

    public void deleteDeviceToken() {
        if (App.getInstance().getUserBean() != null) {
            Timber.d("**************************   delete google token to server   **************************");
            DeleteDeviceTokenBeanReq req = new DeleteDeviceTokenBeanReq();
            req.setUid(App.getInstance().getUserBean().getUid());
            Observable<DeviceTokenBeanRsp> deviceTokenBeanRspObservable = HttpRequest.getInstance().deleteDeviceToken(App.getInstance().getUserBean().getToken(), req);
            ObservableDecorator.decorate(deviceTokenBeanRspObservable).safeSubscribe(new Observer<DeviceTokenBeanRsp>() {
                @Override
                public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                }

                @Override
                public void onNext(@io.reactivex.annotations.NonNull DeviceTokenBeanRsp deviceTokenBeanRsp) {

                }

                @Override
                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                }

                @Override
                public void onComplete() {


                }
            });
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
        //清理设备信息
        removeDeviceList();

        //清理电子围栏信息
        if (null != App.getInstance().getLockGeoFenceService()) {
            App.getInstance().getLockGeoFenceService().clearBleDevice();
        }
        App.getInstance().removeRecords(null);

        User user = App.getInstance().getUser();
        deleteDeviceToken();
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

    //test
}
