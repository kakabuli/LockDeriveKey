package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.a1anwang.okble.client.scan.DeviceScanCallBack;
import com.a1anwang.okble.client.scan.OKBLEScanManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.GetPwd1BeanReq;
import com.revolo.lock.bean.request.LockIsBindBeanReq;
import com.revolo.lock.bean.respone.AdminAddDeviceBeanRsp;
import com.revolo.lock.bean.respone.GetPwd1BeanRsp;
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockConnected;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageReplyErrCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 连接蓝牙或者搜寻到对应的蓝牙连接
 */
public class AddDeviceStep2BleConnectActivity extends BaseActivity {
    private static final int MSG_BLE_ADDDEVICE_CONNECT_FAIL = 102;//ble连接校验超时
    private static final int MSG_BLE_SCAN_OUT_TIME = 103;//搜索超时
    private static final int MSG_BLE_DATA_VALUE_ERR = 104;//sn错误或是mac错误
    private static final int MSG_BLE_SCAN_TIME = 20000;//搜索时间
    private static final int MSG_BLE_CONNECT_FAIL = 105;
    private OKBLEScanManager mScanManager;
    private BLEScanResult mScanResult;

    private static final int mQRPre = 1;
    private static final int mDefault = 0;
    private static final int mESNPre = 2;
    private int mPreA = mDefault;
    private String mEsn;
    private String mMac;
    private boolean isRestartConnectingBle = true;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (!intent.hasExtra(Constant.PRE_A)) return;
        String preA = intent.getStringExtra(Constant.PRE_A);
        if (preA.equals(Constant.INPUT_ESN_A)) {
            initDataFromEsnPre(intent);
        } else if (preA.equals(Constant.QR_CODE_A)) {
            initDataFromQRPre(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initDataFromQRPre(Intent intent) {
        mPreA = mQRPre;
        if (!intent.hasExtra(Constant.QR_RESULT)) return;
        String qrResult = intent.getStringExtra(Constant.QR_RESULT);
        if (TextUtils.isEmpty(qrResult)) return;
        String[] list = qrResult.split("&");
        Timber.d("initData QR Code: %1s", qrResult);
        if (list.length == 2) {
            // 分成两份是正确的
            // ESN=S420210110001&MAC=10:98:C3:72:C6:23
            mEsn = list[0].substring(4).trim();
            mMac = list[1].substring(4).trim();
            Timber.d("initData Mac: %1s", mMac);
        }
    }

    private void initDataFromEsnPre(Intent intent) {
        mPreA = mESNPre;
        if (!intent.hasExtra(Constant.ESN)) return;
        mEsn = intent.getStringExtra(Constant.ESN);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_step2_ble_connect;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        onRegisterEventBus();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_BLE_SCAN_OUT_TIME:
                case MSG_BLE_DATA_VALUE_ERR:
                case MSG_BLE_ADDDEVICE_CONNECT_FAIL:
                    //搜索超时
                    gotoBleConnectFail();
                    break;
                case MSG_BLE_CONNECT_FAIL:
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            //user 操作
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //成功
                if (lockMessage.getMessageCode() == LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE) {
                    Timber.d("addDevice getEventBus");
                    addDeviceToService(lockMessage.getBleResultBea());
                }
            } else {
                //异常
                switch (lockMessage.getResultCode()) {
                    case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_CONNECT_OUT_TIME://连接超时
                    case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_CHECK_ERR://校验失败
                        //  case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_WRITE_ERR://写入失败
                        // case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_DATA_NOTIFY_ERR://通知失败
                        //case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_BLE_DIS_ERR://蓝牙断开失败
                        //case LockMessageReplyErrCode.LOCK_BLE_ERR_CODE_BLE_VALUE_ERR://蓝牙蓝牙数据错误
                        gotoBleConnectFail();
                        break;
                }
            }
        } else {
            //MQTT
        }
    }


    @Override
    public void doBusiness() {
        mCountDownTimer.start();
        if (mPreA == mQRPre || mPreA == mESNPre) {
            checkDeviceIsBind();
        } else {
            gotoBleConnectFail();
        }
    }

    private final CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (!isRestartConnectingBle) {
                handler.sendEmptyMessage(MSG_BLE_CONNECT_FAIL);
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            gotoBleConnectFail();
        }
    };

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScanManager != null && !mScanManager.isScanning()) {
            mScanManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        if (mScanManager != null) {
            mScanManager.stopScan();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        BleBean bleBean = App.getInstance().getUserBleBean(mMac);
        //替换
        //BleBean bleBean = App.getInstance().getBleBeanFromMac(mMac);
        if (bleBean != null) {
            bleBean.setAppPair(false);
        }
        handler.sendEmptyMessage(MSG_BLE_CONNECT_FAIL);
        handler.removeMessages(MSG_BLE_ADDDEVICE_CONNECT_FAIL);
        super.onDestroy();
    }

    private void checkDeviceIsBind() {
        if (!checkNetConnectFail() || App.getInstance() == null || App.getInstance().getUserBean() == null) {
            Timber.e("App.getInstance().getUserBean() == null");
            gotoBleConnectFail();
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("uid is empty");
            gotoBleConnectFail();
            return;
        }
        LockIsBindBeanReq req = new LockIsBindBeanReq();
        req.setDeviceSN(mEsn);
        req.setUser_id(uid);
        Observable<LockIsBindBeanRsp> observable = HttpRequest.getInstance()
                .lockIsBind(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LockIsBindBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LockIsBindBeanRsp lockIsBindBeanRsp) {
                processBindDevice(lockIsBindBeanRsp);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
                gotoBleConnectFail();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void processBindDevice(@NonNull LockIsBindBeanRsp lockIsBindBeanRsp) {
        String code = lockIsBindBeanRsp.getCode();
        if (code == null) {
            return;
        }
        if (code.equals("444")) {
            App.getInstance().logout(true, AddDeviceStep2BleConnectActivity.this);
            return;
        }
        if (code.equals("202")) {
            // 提示已绑定，并退出
            // TODO: 2021/2/6 修改显示方式
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_the_device_is_already_bound);
            finishPreAct();
            finish();
            return;
        }
        if (code.equals("201")) {
            getPwd1FromNet();
            return;
        }
        Timber.e("code: %1s，msg: %2s", lockIsBindBeanRsp.getCode(), lockIsBindBeanRsp.getMsg());
    }

    private void finishPreAct() {
        ActivityUtils.finishActivity(AddDeviceActivity.class);
        ActivityUtils.finishActivity(AddDeviceStep1Activity.class);
        ActivityUtils.finishActivity(AddDeviceQRCodeStep2Activity.class);
    }

    private byte[] mPwd1;

    private void getPwd1FromNet() {
        if (!checkNetConnectFail()) {
            return;
        }
        GetPwd1BeanReq req = new GetPwd1BeanReq();
        req.setSN(mEsn);
        Observable<GetPwd1BeanRsp> getPwd1BeanRspObservable = HttpRequest
                .getInstance().getPwd1(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(getPwd1BeanRspObservable).safeSubscribe(new Observer<GetPwd1BeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetPwd1BeanRsp getPwd1BeanRsp) {
                processGetPwd1(getPwd1BeanRsp);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
                gotoBleConnectFail();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void processGetPwd1(@NonNull GetPwd1BeanRsp getPwd1BeanRsp) {
        // TODO: 2021/1/26 添加错误操作
        String code = getPwd1BeanRsp.getCode();
        if (!code.equals("200")) {
            if (code.equals("444")) {
                App.getInstance().logout(true, AddDeviceStep2BleConnectActivity.this);
                return;
            }
            String msg = getPwd1BeanRsp.getMsg();
            Timber.e("getPwd1FromNet code: %1s, msg: %2s", getPwd1BeanRsp.getCode(), msg);
            if (!TextUtils.isEmpty(msg)) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
            }
            gotoBleConnectFail();
            return;
        }
        if (getPwd1BeanRsp.getData() == null) {
            Timber.e("getPwd1FromNet getPwd1BeanRsp.getData() == null");
            gotoBleConnectFail();
            return;
        }
        if (TextUtils.isEmpty(getPwd1BeanRsp.getData().getPassword1())) {
            Timber.e("getPwd1FromNet getPwd1BeanRsp.getData().getPassword1() is empty");
            gotoBleConnectFail();
            return;
        }
        byte[] bytes = ConvertUtils.hexString2Bytes(getPwd1BeanRsp.getData().getPassword1());
        if (bytes.length != 12) {
            Timber.e("getPwd1FromNet bytes.length != 12");
            return;
        }
        mPwd1 = new byte[16];
        System.arraycopy(bytes, 0, mPwd1, 0, bytes.length);
        initScanManager();
    }


    private void addDeviceToLocal(int power, @NotNull String esn,
                                  @NotNull String mac,
                                  @NotNull String pwd1,
                                  @NotNull String pwd2,
                                  @NotNull BLEScanResult scanResultJson) {
        User user = App.getInstance().getUser();
        if (user != null) {
            BleDeviceLocal bleDeviceLocal = new BleDeviceLocal();
            bleDeviceLocal.setEsn(esn);
            bleDeviceLocal.setMac(mac);
            bleDeviceLocal.setPwd1(pwd1);
            bleDeviceLocal.setPwd2(pwd2);
            // 设置初始默认值为30s
            bleDeviceLocal.setSetAutoLockTime(30);
            bleDeviceLocal.setDoorSensor(LocalState.DOOR_SENSOR_INIT);
            bleDeviceLocal.setUserId(user.getAdminUid());
            bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
            bleDeviceLocal.setLockState(LocalState.LOCK_STATE_CLOSE);
            bleDeviceLocal.setMute(false);
            bleDeviceLocal.setLockPower(power);
            bleDeviceLocal.setAutoLock(true);
            bleDeviceLocal.setScanResultJson(ConvertUtils.parcelable2Bytes(scanResultJson));
            // 统一使用秒，所以毫秒要除以1000
            bleDeviceLocal.setCreateTime(TimeUtils.getNowMills() / 1000);
            long deviceId = AppDatabase.getInstance(this).bleDeviceDao().insert(bleDeviceLocal);
            BleDeviceLocal deviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(deviceId);
            Timber.d("addDeviceToLocal autoTime: %1d", bleDeviceLocal.getSetAutoLockTime());
            App.getInstance().setBleDeviceLocal(deviceLocal);
            //  App.getInstance().addBleDeviceLocal(deviceLocal);
        }
    }

    private void addDeviceToService(BleResultBean bleResultBean) {
        if (!checkNetConnectFail()) {
            gotoBleConnectFail();
            return;
        }
        BleBean bleBean = App.getInstance().getUserBleBean(bleResultBean.getScanResult().getMacAddress());
        if (null != bleBean) {
            // 本地存储
            int power = BleByteUtil.byteToInt(bleResultBean.getPayload()[11]);
            addDeviceToLocal(power, mEsn, mMac, ConvertUtils.bytes2HexString(mPwd1), ConvertUtils.bytes2HexString(bleBean.getPwd2()), mScanResult);
            AdminAddDeviceBeanReq req = new AdminAddDeviceBeanReq();
            req.setDevmac(mMac);
            req.setDeviceSN(mEsn);
            req.setModel(getModeTypeFromBleManufacturerSpecificData(mScanResult));
            req.setUser_id(App.getInstance().getUserBean().getUid());
            // 正确的是12位pwd1,因为在内存里的pwd1是补0了，所以是16位，但是传输到服务器的需要移除0
            byte[] realPwd1 = new byte[12];
            System.arraycopy(mPwd1, 0, realPwd1, 0, realPwd1.length);
            req.setPassword1(ConvertUtils.bytes2HexString(realPwd1));
            req.setPassword2(ConvertUtils.bytes2HexString(bleBean.getPwd2()));
            Timber.d("addDeviceToService req: %1s", req.toString());
            Observable<AdminAddDeviceBeanRsp> observable = HttpRequest
                    .getInstance().adminAddDevice(App.getInstance().getUserBean().getToken(), req);
            ObservableDecorator.decorate(observable).safeSubscribe(new Observer<AdminAddDeviceBeanRsp>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull AdminAddDeviceBeanRsp adminAddDeviceBeanRsp) {
                    processAddDevice(adminAddDeviceBeanRsp);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Timber.e(e);
                }

                @Override
                public void onComplete() {

                }
            });
        } else {
            //异常
        }


    }

    private void processAddDevice(@NonNull AdminAddDeviceBeanRsp adminAddDeviceBeanRsp) {
        String code = adminAddDeviceBeanRsp.getCode();
        if (code == null) {
            gotoBleConnectFail();
            return;
        }
        if (!code.equals("200")) {
            if (code.equals("444")) {
                App.getInstance().logout(true, AddDeviceStep2BleConnectActivity.this);
                return;
            }
            Timber.e("code: %1s, msg: %2s", adminAddDeviceBeanRsp.getCode(), adminAddDeviceBeanRsp.getMsg());
            return;
        }
        Timber.d("addDeviceToService 添加设备成功");
        Timber.d("rsp: %1s", adminAddDeviceBeanRsp.toString());
        Intent intent = new Intent(AddDeviceStep2BleConnectActivity.this, BleConnectSucActivity.class);
        startActivity(intent);
        finish();
    }

    private void initScanManager() {
        mScanManager = App.getInstance().getScanManager();
        DeviceScanCallBack scanCallBack = new DeviceScanCallBack() {
            @Override
            public void onBLEDeviceScan(BLEScanResult device, int rssi) {
                filterAndConnectBle(device);
            }

            @Override
            public void onFailed(int code) {
                bleScanFailed(code);
                gotoBleConnectFail();
            }

            @Override
            public void onStartSuccess() {
            }
        };
        App.getInstance().setScanCallBack(scanCallBack);
        mScanManager.startScan();
        handler.sendEmptyMessageDelayed(MSG_BLE_SCAN_OUT_TIME, MSG_BLE_SCAN_TIME);
    }

    private void filterAndConnectBle(BLEScanResult device) {
        if (!TextUtils.isEmpty(device.getCompleteLocalName()) && device.getCompleteLocalName().contains("Rev")) {
            Timber.d(" scan: name:%1s  mac: %2s",
                    device.getCompleteLocalName(), device.getMacAddress());
            if (mPreA == mQRPre) {
                connectBleFromQRCode(device);
            } else if (mPreA == mESNPre) {
                connectBleFromInputEsn(device);
            } else {
                gotoBleConnectFail();
            }
        }
    }

    private void gotoBleConnectFail() {
        handler.removeMessages(MSG_BLE_SCAN_OUT_TIME);
        handler.removeMessages(MSG_BLE_ADDDEVICE_CONNECT_FAIL);
        Intent intent = new Intent(this, BleConnectFailActivity.class);
        Intent preIntent = getIntent();
        if (!preIntent.hasExtra(Constant.PRE_A)) return;
        String preA = preIntent.getStringExtra(Constant.PRE_A);
        intent.putExtra(Constant.PRE_A, preA);
        if (preA.equals(Constant.INPUT_ESN_A)) {
            preIntent.putExtra(Constant.ESN, preIntent.getStringExtra(Constant.ESN));
            intent.putExtra(Constant.ESN, preIntent.getStringExtra(Constant.ESN));
        } else if (preA.equals(Constant.QR_CODE_A)) {
            preIntent.putExtra(Constant.QR_RESULT, preIntent.getStringExtra(Constant.QR_RESULT));
            intent.putExtra(Constant.QR_RESULT, preIntent.getStringExtra(Constant.QR_RESULT));
        }
        BleBean bleBean = App.getInstance().getUserBleBean(mMac);
        if (null != bleBean) {
            App.getInstance().removeConnectedBleDisconnect(bleBean);
        }
        //替换
       /* BleBean bleBean = App.getInstance().getBleBeanFromMac(mMac);
        App.getInstance().removeConnectedBleBeanAndDisconnect(bleBean);*/
        startActivity(intent);
        finish();
    }

    private void bleScanFailed(int code) {
        switch (code) {
            case DeviceScanCallBack.SCAN_FAILED_BLE_NOT_SUPPORT:
                // TODO 2021/1/22 设备不支持
                Timber.e("该设备不支持BLE");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("The device does not support Bluetooth");
                finish();
                break;
            case DeviceScanCallBack.SCAN_FAILED_BLUETOOTH_DISABLE:
                // TODO: 2021/1/22 打开手机蓝牙
                Timber.e("请打开手机蓝牙");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_open_bluetooth);
                finish();
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE:
                // TODO: 2021/1/22 请求位置权限
                Timber.e("请授予位置权限以扫描周围的蓝牙设备");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_open_location);
                finish();
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE_FOREVER:
                // TODO: 2021/1/22 跳转到授予位置权限的页面
                Timber.e("位置权限被您永久拒绝,请在设置里授予位置权限以扫描周围的蓝牙设备");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Please open location permission");
                finish();
                break;
        }
    }

    private void connectBleFromInputEsn(BLEScanResult device) {
        if (TextUtils.isEmpty(mEsn)) {
            handler.sendEmptyMessage(MSG_BLE_DATA_VALUE_ERR);
            return;
        }
        if (isDeviceEsnEqualsInputEsn(device, mEsn)) {
            handler.removeMessages(MSG_BLE_SCAN_OUT_TIME);
            mScanManager.stopScan();
            mScanResult = device;
            mMac = device.getMacAddress();
            LockConnected bleConnected = new LockConnected();
            bleConnected.setConnectType(1);
            bleConnected.setAppPair(true);
            bleConnected.setPwd1(mPwd1);
            bleConnected.setPwd2(null);
            bleConnected.setmEsn(mEsn);
            bleConnected.setBleScanResult(device);
            EventBus.getDefault().post(bleConnected);
            handler.removeMessages(MSG_BLE_ADDDEVICE_CONNECT_FAIL);
            handler.sendEmptyMessageDelayed(MSG_BLE_ADDDEVICE_CONNECT_FAIL, 15000);
        }
    }

    private void connectBleFromQRCode(BLEScanResult device) {
        if (TextUtils.isEmpty(mMac)) {
            handler.sendEmptyMessage(MSG_BLE_DATA_VALUE_ERR);
            return;
        }
        if (device.getMacAddress().equalsIgnoreCase(mMac)) {
            handler.removeMessages(MSG_BLE_SCAN_OUT_TIME);
            mScanManager.stopScan();
            Timber.d("connectBleFromQRCode 扫描到设备");
            mScanResult = device;
            LockConnected bleConnected = new LockConnected();
            bleConnected.setAppPair(true);
            bleConnected.setPwd1(mPwd1);
            bleConnected.setPwd2(null);
            bleConnected.setConnectType(1);
            bleConnected.setmEsn(mEsn);
            bleConnected.setBleScanResult(device);
            EventBus.getDefault().post(bleConnected);
            handler.removeMessages(MSG_BLE_ADDDEVICE_CONNECT_FAIL);
            handler.sendEmptyMessageDelayed(MSG_BLE_ADDDEVICE_CONNECT_FAIL, 30000);
        }
    }

    private String getModeTypeFromBleManufacturerSpecificData(BLEScanResult result) {
        String modeType = "";
        //返回Manufacture ID之后的data
        SparseArray<byte[]> hex16 = result.getManufacturerSpecificData();
        if (hex16 != null && hex16.size() > 0) {
            byte[] value = hex16.valueAt(0);
            //过滤无用蓝牙广播数据
            if (value.length < 16) return modeType;
            //截取出mode
            StringBuilder sb = new StringBuilder();
            for (int j = 16; j < 21; j++) {
                sb.append((char) value[j]);
            }
            modeType = sb.toString().trim();
            return modeType;
        }
        return modeType;
    }

    private boolean isDeviceEsnEqualsInputEsn(BLEScanResult device, String esn) {
        // TODO: 2021/2/8 判断长度
        //返回Manufacture ID之后的data
        SparseArray<byte[]> hex16 = device.getManufacturerSpecificData();
        if (hex16 != null && hex16.size() > 0) {
            byte[] value = hex16.valueAt(0);
            //过滤无用蓝牙广播数据
            if (value.length < 16) return false;
            //截取出SN
            StringBuilder sb = new StringBuilder();
            for (int j = 3; j < 16; j++) {
                sb.append((char) value[j]);
            }
            String sn = sb.toString().trim();
            Timber.d("isDeviceEsnEqualsInputEsn %1s, %2s, len: %3d", ConvertUtils.bytes2HexString(value), new String(value, StandardCharsets.UTF_8), value.length);
            Timber.d("isDeviceEsnEqualsInputEsn device esn: %1s, input esn: %2s, mac: %3s", sn, esn, device.getMacAddress());
            return sn.equalsIgnoreCase(esn);
        }
        return false;
    }

}
