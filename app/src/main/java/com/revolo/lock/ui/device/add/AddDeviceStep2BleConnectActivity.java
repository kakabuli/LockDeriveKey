package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;
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
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.room.entity.User;

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

    private OKBLEScanManager mScanManager;
    private BLEScanResult mScanResult;

    private static final int mQRPre = 1;
    private static final int mDefault = 0;
    private static final int mESNPre = 2;
    private int mPreA = mDefault;
    private String mEsn;
    private String mMac;

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
        if(list.length == 2) {
            // 分成两份是正确的
            // ESN=S420210110001&MAC=10:98:C3:72:C6:23
            mEsn = list[0].substring(4).trim();
            mMac = list[1].substring(4).trim();
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

    }

    @Override
    public void doBusiness() {
        if(mPreA == mQRPre || mPreA == mESNPre) {
            checkDeviceIsBind();
        } else {
            gotoBleConnectFail();
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
        if(mScanManager != null) {
            mScanManager.stopScan();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mMac);
        if(bleBean != null) {
            bleBean.setAppPair(false);
        }
        super.onDestroy();
    }

    private void checkDeviceIsBind() {
        if(!checkNetConnectFail()) {
            return;
        }
        if(App.getInstance()  == null) {
            Timber.e("App.getInstance()  == null");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("uid is empty");
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
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void processBindDevice(@NonNull LockIsBindBeanRsp lockIsBindBeanRsp) {
        String code = lockIsBindBeanRsp.getCode();
        if(code == null) {
            return;
        }
        if(code.equals("444")) {
            App.getInstance().logout(true, AddDeviceStep2BleConnectActivity.this);
            return;
        }
        if(code.equals("202")) {
            // 提示已绑定，并退出
            // TODO: 2021/2/6 修改显示方式
            ToastUtils.showLong(R.string.t_the_device_is_already_bound);
            finishPreAct();
            finish();
            return;
        }
        if(code.equals("201")) {
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
        if(!checkNetConnectFail()) {
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
        if(!code.equals("200")) {
            if(code.equals("444")) {
                App.getInstance().logout(true, AddDeviceStep2BleConnectActivity.this);
                return;
            }
            String msg = getPwd1BeanRsp.getMsg();
            Timber.e("getPwd1FromNet code: %1s, msg: %2s", getPwd1BeanRsp.getCode(), msg);
            if(!TextUtils.isEmpty(msg)) {
                ToastUtils.showShort(msg);
            }
            gotoBleConnectFail();
            return;
        }
        if(getPwd1BeanRsp.getData() == null) {
            Timber.e("getPwd1FromNet getPwd1BeanRsp.getData() == null");
            gotoBleConnectFail();
            return;
        }
        if(TextUtils.isEmpty(getPwd1BeanRsp.getData().getPassword1())) {
            Timber.e("getPwd1FromNet getPwd1BeanRsp.getData().getPassword1() is empty");
            gotoBleConnectFail();
            return;
        }
        byte[] bytes = ConvertUtils.hexString2Bytes(getPwd1BeanRsp.getData().getPassword1());
        if(bytes.length != 12) {
            Timber.e("getPwd1FromNet bytes.length != 12");
            return;
        }
        mPwd1 = new byte[16];
        System.arraycopy(bytes, 0, mPwd1, 0, bytes.length);
        initScanManager();
    }

    /**               蓝牙               **/

    private boolean isHavePwd2Or3 = false;
    private byte[] mPwd2;
    private byte[] mPwd3;
    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        auth(bleResultBean);
    };

    private void auth(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == BleProtocolState.CMD_PAIR_ACK) {
            if(bleResultBean.getPayload()[0] != 0x00) {
                // 校验失败
                Timber.e("校验失败 CMD: %1s, 回复的数据：%2s",
                        ConvertUtils.int2HexString(bleResultBean.getCMD()), ConvertUtils.bytes2HexString(bleResultBean.getPayload()));
                gotoBleConnectFail();
            }
            return;
        }
        processKey(bleResultBean);
    }

    private void processKey(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == BleProtocolState.CMD_ENCRYPT_KEY_UPLOAD) {
            byte[] data = bleResultBean.getPayload();
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mMac);
            if(bleBean == null) {
                Timber.e("processKey bleBean == null");
                return;
            }
            if(data[0] == 0x01) {
                // 入网时
                // 获取pwd2
                getPwd2AndSendAuthCommand(bleResultBean, data, bleBean);
            } else if(data[0] == 0x02) {
                // 获取pwd3
                getPwd3(bleResultBean, data, bleBean);
                // 鉴权成功后，同步当前时间
                syNowTime(bleBean);
                addDeviceToService();
            }
        }
    }

    private void syNowTime(@NotNull BleBean bleBean) {
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("syNowTime bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, mPwd1, mPwd3), bleBean.getOKBLEDeviceImp());
        }, 20);
    }

    private void getPwd3(BleResultBean bleResultBean, byte[] data, @NotNull BleBean bleBean) {
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("getPwd3 bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        mPwd3 = new byte[4];
        System.arraycopy(data, 1, mPwd3, 0, mPwd3.length);
        Timber.d("getPwd3 鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd3));
        // 内存存储
        bleBean.setPwd1(mPwd1);
        bleBean.setPwd3(mPwd3);
        App.getInstance().writeControlMsg(BleCommandFactory
                .ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
    }

    private void addDeviceToLocal(@NotNull String esn,
                                  @NotNull String mac,
                                  @NotNull String pwd1,
                                  @NotNull String pwd2,
                                  @NotNull BLEScanResult scanResultJson) {
        User user = App.getInstance().getUser();
        if(user != null) {
            BleDeviceLocal bleDeviceLocal = new BleDeviceLocal();
            bleDeviceLocal.setEsn(esn);
            bleDeviceLocal.setMac(mac);
            bleDeviceLocal.setPwd1(pwd1);
            bleDeviceLocal.setPwd2(pwd2);
            // 设置初始默认值为30s
            bleDeviceLocal.setSetAutoLockTime(30);
            bleDeviceLocal.setDoorSensor(LocalState.DOOR_SENSOR_INIT);
            bleDeviceLocal.setUserId(user.getId());
            bleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
            bleDeviceLocal.setLockState(LocalState.LOCK_STATE_CLOSE);
            bleDeviceLocal.setScanResultJson(ConvertUtils.parcelable2Bytes(scanResultJson));
            // 统一使用秒，所以毫秒要除以1000
            bleDeviceLocal.setCreateTime(TimeUtils.getNowMills()/1000);
            long deviceId = AppDatabase.getInstance(this).bleDeviceDao().insert(bleDeviceLocal);
            BleDeviceLocal deviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(deviceId);
            App.getInstance().setBleDeviceLocal(deviceLocal);
            App.getInstance().addBleDeviceLocal(deviceLocal);
        }
    }

    private void getPwd2AndSendAuthCommand(BleResultBean bleResultBean, byte[] data, @NotNull BleBean bleBean) {
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("getPwd2AndSendAuthCommand bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        mPwd2 = new byte[4];
        System.arraycopy(data, 1, mPwd2, 0, mPwd2.length);
        // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
        isHavePwd2Or3 = true;
        bleBean.setPwd2(mPwd2);
        bleBean.setEsn(mEsn);
        App.getInstance().writeControlMsg(BleCommandFactory
                .ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()), bleBean.getOKBLEDeviceImp());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
            App.getInstance().writeControlMsg(BleCommandFactory
                    .authCommand(mPwd1, mPwd2, mEsn.getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
        }, 50);
    }

    private void addDeviceToService()  {
        if(!checkNetConnectFail()) {
            return;
        }
        // 本地存储
        addDeviceToLocal(mEsn, mMac, ConvertUtils.bytes2HexString(mPwd1), ConvertUtils.bytes2HexString(mPwd2), mScanResult);
        AdminAddDeviceBeanReq req = new AdminAddDeviceBeanReq();
        req.setDevmac(mMac);
        req.setDeviceSN(mEsn);
        req.setModel(getModeTypeFromBleManufacturerSpecificData(mScanResult));
        req.setUser_id(App.getInstance().getUserBean().getUid());
        // 正确的是12位pwd1,因为在内存里的pwd1是补0了，所以是16位，但是传输到服务器的需要移除0
        byte[] realPwd1 = new byte[12];
        System.arraycopy(mPwd1, 0, realPwd1, 0, realPwd1.length);
        req.setPassword1(ConvertUtils.bytes2HexString(realPwd1));
        req.setPassword2(ConvertUtils.bytes2HexString(mPwd2));
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

    }

    private void processAddDevice(@NonNull AdminAddDeviceBeanRsp adminAddDeviceBeanRsp) {
        String code = adminAddDeviceBeanRsp.getCode();
        if(code == null) {
            gotoBleConnectFail();
            return;
        }
        if(!code.equals("200")) {
            if(code.equals("444")) {
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

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {
            processDeviceConnected(mac);
        }

        @Override
        public void onDisconnected(@NotNull String mac) {

        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            processRecValue(value);
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }
    };

    private void processDeviceConnected(@NotNull String mac) {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mac);
        if(bleBean == null) {
            Timber.e("initBleListener bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Timber.d("%1s 发送配网指令，并校验ESN", mac);
            App.getInstance().writeControlMsg(BleCommandFactory
                    .pairCommand(mPwd1, mEsn.getBytes(StandardCharsets.UTF_8)), bleBean.getOKBLEDeviceImp());
        }, 100);
    }

    private void processRecValue(byte[] value) {
        if(value == null) {
            return;
        }
        BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
        byte[] pwd2Or3 = null;
        if(isHavePwd2Or3) {
            if(mPwd3 == null) {
                if(mPwd2 != null) {
                    pwd2Or3 = mPwd2;
                }
            } else {
                pwd2Or3 = mPwd3;
            }
        }
        BleResultProcess.processReceivedData(value, mPwd1, isHavePwd2Or3?pwd2Or3:null, mScanResult);
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
            }

            @Override
            public void onStartSuccess() {
            }
        };
        App.getInstance().setScanCallBack(scanCallBack);
        mScanManager.startScan();
    }

    private void filterAndConnectBle(BLEScanResult device) {
        if(!TextUtils.isEmpty(device.getCompleteLocalName())&&device.getCompleteLocalName().contains("Rev")) {
            Timber.d(" scan: name:%1s  mac: %2s",
                    device.getCompleteLocalName(),  device.getMacAddress());
            if(mPreA == mQRPre) {
                connectBleFromQRCode(device);
            } else if(mPreA == mESNPre) {
                connectBleFromInputEsn(device);
            } else {
                gotoBleConnectFail();
            }
        }
    }

    private void gotoBleConnectFail() {
        Intent intent = new Intent(this, BleConnectFailActivity.class);
        Intent preIntent = getIntent();
        if(!preIntent.hasExtra(Constant.PRE_A)) return;
        String preA = preIntent.getStringExtra(Constant.PRE_A);
        intent.putExtra(Constant.PRE_A, preA);
        if(preA.equals(Constant.INPUT_ESN_A)) {
            preIntent.putExtra(Constant.ESN, intent.getStringExtra(Constant.ESN));
        } else if(preA.equals(Constant.QR_CODE_A)) {
            preIntent.putExtra(Constant.QR_RESULT, intent.getStringExtra(Constant.QR_RESULT));
        }
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mMac);
        App.getInstance().removeConnectedBleBeanAndDisconnect(bleBean);
        startActivity(intent);
        finish();
    }

    private void bleScanFailed(int code) {
        switch (code){
            case DeviceScanCallBack.SCAN_FAILED_BLE_NOT_SUPPORT:
                Timber.e("该设备不支持BLE");
                break;
            case DeviceScanCallBack.SCAN_FAILED_BLUETOOTH_DISABLE:
                Timber.e("请打开手机蓝牙");
                // TODO: 2021/1/22 打开手机蓝牙
                ToastUtils.showShort(R.string.t_please_open_bluetooth);
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE:
                Timber.e("请授予位置权限以扫描周围的蓝牙设备");
                // TODO: 2021/1/22 请求位置权限
                ToastUtils.showShort(R.string.t_please_open_location);
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE_FOREVER:
                Timber.e("位置权限被您永久拒绝,请在设置里授予位置权限以扫描周围的蓝牙设备");
                // TODO: 2021/1/22 跳转到授予位置权限的页面
                break;
        }
    }

    private void connectBleFromInputEsn(BLEScanResult device) {
        if(TextUtils.isEmpty(mEsn)) return;
        if(isDeviceEsnEqualsInputEsn(device, mEsn)) {
            mScanManager.stopScan();
            mScanResult = device;
            mMac = device.getMacAddress();
            App.getInstance().connectDevice(device, mPwd1, mPwd2, mOnBleDeviceListener,true);
        }
    }

    private void connectBleFromQRCode(BLEScanResult device) {
        if(TextUtils.isEmpty(mMac)) return;
        if(device.getMacAddress().equalsIgnoreCase(mMac)) {
            mScanManager.stopScan();
            Timber.d("connectBleFromQRCode 扫描到设备");
            mScanResult = device;
            App.getInstance().connectDevice(device, mPwd1, mPwd2, mOnBleDeviceListener,true);
        }
    }

    private String getModeTypeFromBleManufacturerSpecificData(BLEScanResult result) {
        String modeType = "";
        //返回Manufacture ID之后的data
        SparseArray<byte[]> hex16 = result.getManufacturerSpecificData();
        if(hex16 != null && hex16.size() > 0) {
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
        if(hex16 != null && hex16.size() > 0) {
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
