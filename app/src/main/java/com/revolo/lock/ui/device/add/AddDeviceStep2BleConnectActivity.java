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
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
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

    private final int mQRPre = 1;
    private final int mDefault = 0;
    private final int mESNPre = 2;
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
        App.getInstance().setAppPair(true);
        if(mPreA == mQRPre || mPreA == mESNPre) {
            checkDeviceIsBind();
//            getPwd1FromNet();
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
        super.onPause();
        if(mScanManager != null && mScanManager.isScanning()) {
            mScanManager.stopScan();
        }
    }

    @Override
    protected void onDestroy() {
        App.getInstance().setAppPair(false);
        if(mScanManager != null && mScanManager.isScanning()) {
            mScanManager.stopScan();
        }
        super.onDestroy();
    }

    private void checkDeviceIsBind() {
        LockIsBindBeanReq req = new LockIsBindBeanReq();
        req.setDeviceSN(mEsn);
        req.setUser_id(App.getInstance().getUserBean().getUid());
        Observable<LockIsBindBeanRsp> observable = HttpRequest.getInstance()
                .lockIsBind(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LockIsBindBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LockIsBindBeanRsp lockIsBindBeanRsp) {
                if(lockIsBindBeanRsp.getCode() == null) {
                    return;
                }
                if(lockIsBindBeanRsp.getCode().equals("202")) {
                    // 提示已绑定，并退出
                    // TODO: 2021/2/6 修改显示方式
                    ToastUtils.showLong("The device is already bound");
                    App.getInstance().finishPreActivities();
                    finish();
                    return;
                }
                if(lockIsBindBeanRsp.getCode().equals("201")) {
                    getPwd1FromNet();
                    return;
                }
                Timber.e("code: %1s，msg: %2s", lockIsBindBeanRsp.getCode(), lockIsBindBeanRsp.getMsg());
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

    private byte[] mPwd1;

    private void getPwd1FromNet() {
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
                // TODO: 2021/1/26 添加错误操作
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
                initBleListener();
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
            if(data[0] == 0x01) {
                // 入网时
                // 获取pwd2
                getPwd2AndSendAuthCommand(bleResultBean, data);
            } else if(data[0] == 0x02) {
                // 获取pwd3
                getPwd3(bleResultBean, data);
                // 鉴权成功后，同步当前时间
                syNowTime();
                addDeviceToService();
            }
        }
    }

    private void syNowTime() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long nowTime = TimeUtils.getNowMills()/1000;
            App.getInstance().writeControlMsg(BleCommandFactory
                    .syLockTime(nowTime, mPwd1, mPwd3));
        }, 20);
    }

    private void getPwd3(BleResultBean bleResultBean, byte[] data) {
        mPwd3 = new byte[4];
        System.arraycopy(data, 1, mPwd3, 0, mPwd3.length);
        Timber.d("getPwd3 鉴权成功, pwd3: %1s\n", ConvertUtils.bytes2HexString(mPwd3));
        // 内存存储
        App.getInstance().getBleBean().setPwd1(mPwd1);
        App.getInstance().getBleBean().setPwd3(mPwd3);
        App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
    }

    private long mDeviceId = -1;

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
            bleDeviceLocal.setUserId(user.getId());
            // TODO: 2021/3/1 后面改成enum，默认关
            bleDeviceLocal.setLockState(2);
            bleDeviceLocal.setScanResultJson(ConvertUtils.parcelable2Bytes(scanResultJson));
            // 统一使用秒，所以毫秒要除以1000
            bleDeviceLocal.setCreateTime(TimeUtils.getNowMills()/1000);
            mDeviceId = AppDatabase.getInstance(this).bleDeviceDao().insert(bleDeviceLocal);
        }
    }

    private void getPwd2AndSendAuthCommand(BleResultBean bleResultBean, byte[] data) {
        mPwd2 = new byte[4];
        System.arraycopy(data, 1, mPwd2, 0, mPwd2.length);
        // TODO: 2021/1/21 打包数据上传到服务器后再发送确认指令
        isHavePwd2Or3 = true;
        App.getInstance().getBleBean().setPwd2(mPwd2);
        App.getInstance().getBleBean().setEsn(mEsn);
        App.getInstance().writeControlMsg(BleCommandFactory.ackCommand(bleResultBean.getTSN(), (byte)0x00, bleResultBean.getCMD()));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Timber.d("getPwd2AndSendAuthCommand 延时发送鉴权指令, pwd2: %1s\n", ConvertUtils.bytes2HexString(mPwd2));
            App.getInstance().writeControlMsg(BleCommandFactory
                    .authCommand(mPwd1, mPwd2, mEsn.getBytes(StandardCharsets.UTF_8)));
        }, 50);
    }

    private void addDeviceToService()  {
        // 本地存储
        addDeviceToLocal(mEsn, mMac, ConvertUtils.bytes2HexString(mPwd1), ConvertUtils.bytes2HexString(mPwd2), mScanResult);
        AdminAddDeviceBeanReq req = new AdminAddDeviceBeanReq();
        req.setDevmac(mMac);
        req.setDeviceSN(mEsn);
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
                if(adminAddDeviceBeanRsp.getCode() == null) {
                    gotoBleConnectFail();
                    return;
                }
                if(!adminAddDeviceBeanRsp.getCode().equals("200")) {
                    Timber.e("code: %1s, msg: %2s", adminAddDeviceBeanRsp.getCode(), adminAddDeviceBeanRsp.getMsg());
                    return;
                }
                Timber.d("addDeviceToService 添加设备成功");
                Timber.d("rsp: %1s", adminAddDeviceBeanRsp.toString());
                Intent intent = new Intent(AddDeviceStep2BleConnectActivity.this, BleConnectSucActivity.class);
                intent.putExtra(Constant.DEVICE_ID, mDeviceId);
                startActivity(intent);
                finish();
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

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {
                Timber.d("发送配网指令，并校验ESN");
                App.getInstance().writeControlMsg(BleCommandFactory
                        .pairCommand(mPwd1, mEsn.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onReceivedValue(String uuid, byte[] value) {
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

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc() {

            }
        });
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
                break;
            case DeviceScanCallBack.SCAN_FAILED_LOCATION_PERMISSION_DISABLE:
                Timber.e("请授予位置权限以扫描周围的蓝牙设备");
                // TODO: 2021/1/22 请求位置权限
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
            App.getInstance().connectDevice(device);
        }
    }

    private void connectBleFromQRCode(BLEScanResult device) {
        if(TextUtils.isEmpty(mMac)) return;
        if(device.getMacAddress().equalsIgnoreCase(mMac)) {
            mScanManager.stopScan();
            Timber.d("connectBleFromQRCode 扫描到设备");
            mScanResult = device;
            App.getInstance().connectDevice(device);
        }
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
            Timber.d("%1s, %2s, len: %3d", ConvertUtils.bytes2HexString(value), new String(value, StandardCharsets.UTF_8), value.length);
            Timber.d("getAndCheckESN device esn: %1s, input esn: %2s, mac: %3s", sn, esn, device.getMacAddress());
            return sn.equalsIgnoreCase(esn);
        }
        return false;
    }

}
