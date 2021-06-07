package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.widget.WifiCircleProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * author :
 * time   : 2020/12/30
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
// TODO: 2021/1/22 添加超时处理
public class WifiConnectActivity extends BaseActivity {

    private String mWifiName;
    private String mWifiPwd;
    private BleBean mBleBean;
    private WifiCircleProgress mWifiCircleProgress;
    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (!intent.hasExtra(Constant.WIFI_NAME)) {
            // TODO: 2021/1/22 没有输入wifi name
            finish();
            return;
        }
        if (!intent.hasExtra(Constant.WIFI_PWD)) {
            // TODO: 2021/1/22 没有输入wifi pwd
            finish();
            return;
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
            return;
        }
        mWifiName = intent.getStringExtra(Constant.WIFI_NAME);
        mWifiPwd = intent.getStringExtra(Constant.WIFI_PWD);


    }

    @Override
    public int bindLayout() {
        return R.layout.activity_wifi_connect;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_connect_wifi));
        mWifiCircleProgress = findViewById(R.id.wifiCircleProgress);
        onRegisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            BleResultBean bleResultBean = lockMessage.getBleResultBea();
            if (null != bleResultBean) {
                Timber.d("cmd: %1s, payload: %2s",
                        ConvertUtils.int2HexString(bleResultBean.getCMD()), ConvertUtils.bytes2HexString(bleResultBean.getPayload()));
                if (bleResultBean.getCMD() == BleProtocolState.CMD_SS_ID_ACK) {
                    writeWifiSn();
                } else if (bleResultBean.getCMD() == BleProtocolState.CMD_PWD_ACK) {
                    writeWifiPwd();
                } else if (bleResultBean.getCMD() == BleProtocolState.CMD_UPLOAD_PAIR_NETWORK_STATE) {
                    if (bleResultBean.getPayload()[0] == 0x00) {
                        // 连接wifi成功
                        changeValue(80);
                    } else if (bleResultBean.getPayload()[0] == 0x01) {
                        // 配网失败
                        gotoWifiPairFail();
                    }
                } else if (bleResultBean.getCMD() == BleProtocolState.CMD_BLE_UPLOAD_PAIR_NETWORK_STATE) {
                    // 连接MQTT成功
                    if (bleResultBean.getPayload()[0] == 0x00) {
                        // 连接wifi成功
                        changeValue(100);
                        App.getInstance().removeConnectedBleDisconnect(mBleBean);
                        /*替换
                        App.getInstance().removeConnectedBleBeanAndDisconnect(mBleBean);*/
                        // 设置为wifi模式
                        mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
                        mBleDeviceLocal.setConnectedWifiName(mWifiName);
                        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                        runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            startActivity(new Intent(WifiConnectActivity.this, AddWifiSucActivity.class));
                            finish();
                        }, 50));
                    } else if (bleResultBean.getPayload()[0] == 0x01) {
                        // 配网失败
                        gotoWifiPairFail();
                    }
                } else {
                    // TODO: 2021/1/22 走其他流程
                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                }
            } else {
                switch (lockMessage.getResultCode()) {

                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        changeValue(0);
        initDevice();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initDevice() {
        mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
       /* 替换
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());*/
        if (mBleBean == null) {
            Timber.e("initDevice mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() != null) {
            //替换
            //App.getInstance().openPairNotify(mBleBean.getOKBLEDeviceImp());
            startSendWifiInfo();
        }
    }

    private void gotoWifiPairFail() {
        App.getInstance().setWifiSettingNeedToCloseBle(false);
        mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
        mBleDeviceLocal.setConnectedWifiName("");
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(WifiConnectActivity.this, AddWifiFailActivity.class);
            intent.putExtra(Constant.WIFI_NAME, mWifiName);
            intent.putExtra(Constant.WIFI_PWD, mWifiPwd);
            startActivity(intent);
            finish();
        }, 50);
    }

    private void changeValue(int value) {
        runOnUiThread(() -> mWifiCircleProgress.setValue(value));
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<byte[]> mWifiSnDataList = new ArrayList<>();
    private int mWifiSnCount = 0;
    private int mWifiPwdCount = 0;
    private final List<byte[]> mWifiPwdDataList = new ArrayList<>();
    private int mWifiSnLen = 0;
    private int mWifiPwdLen = 0;
    private boolean isStartSend = false;

    private void startSendWifiInfo() {
        mWifiSnDataList.clear();
        mWifiPwdDataList.clear();
        mWifiSnCount = 0;
        mWifiPwdCount = 0;
        mWifiSnLen = 0;
        mWifiPwdLen = 0;
        isStartSend = true;
        splitWifiName();
        splitWifiPwd();
        writeWifiSn();

    }

    private void splitWifiName() {
        byte[] wifiSnBytes = mWifiName.getBytes(StandardCharsets.UTF_8);
        Timber.d("startSendWifiInfo WifiSn: %1s\n", ConvertUtils.bytes2HexString(wifiSnBytes));
        mWifiSnLen = wifiSnBytes.length;
        for (int i = 0; i < mWifiSnLen; i = i + 14) {
            int maxIndex = Math.min((mWifiSnLen - i), 14);
            byte[] data = new byte[maxIndex];
            System.arraycopy(wifiSnBytes, i, data, 0, data.length);
            Timber.d("startSendWifiInfo mWifiSnLen data %1s, i: %2d", ConvertUtils.bytes2HexString(data), i);
            mWifiSnDataList.add(data);
        }
    }

    private void splitWifiPwd() {
        byte[] wifiPwdBytes;
        if (TextUtils.isEmpty(mWifiPwd)) {
            // 密码为空的时候，都设置为0xff
            // TODO: 2021/2/7 需要验证没有密码的情况
            wifiPwdBytes = new byte[14];
            for (int i = 0; i < 14; i++) {
                wifiPwdBytes[i] = (byte) 0xff;
            }
        } else {
            wifiPwdBytes = mWifiPwd.getBytes(StandardCharsets.UTF_8);
        }
        Timber.d("startSendWifiInfo WifiPwd: %1s\n", ConvertUtils.bytes2HexString(wifiPwdBytes));
        mWifiPwdLen = wifiPwdBytes.length;
        for (int i = 0; i < mWifiPwdLen; i = i + 14) {
            int maxIndex = Math.min((mWifiPwdLen - i), 14);
            byte[] data = new byte[maxIndex];
            System.arraycopy(wifiPwdBytes, i, data, 0, data.length);
            Timber.d("mWifiPwdLen data %1s, i: %2d", ConvertUtils.bytes2HexString(data), i);
            mWifiPwdDataList.add(data);
        }
    }

    private final Runnable mWriteWifiSnRunnable = () -> {
        if (mBleBean == null) {
            Timber.e("mWriteWifiSnRunnable mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() == null) {
            Timber.e("mWriteWifiSnRunnable mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] data = mWifiSnDataList.get(0);
        Timber.d("mWriteWifiSnRunnable data %1s", ConvertUtils.bytes2HexString(data));
        LockMessage lockMessage = new LockMessage();
        lockMessage.setBytes(BleCommandFactory.sendSSIDCommand((byte) mWifiSnLen, (byte) mWifiSnCount, data));
        lockMessage.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
        lockMessage.setMessageType(3);
        lockMessage.setBleChr(1);
        EventBus.getDefault().post(lockMessage);
     /*   替换
        App.getInstance().writePairMsg(BleCommandFactory.sendSSIDCommand((byte) mWifiSnLen, (byte) mWifiSnCount, data), mBleBean.getOKBLEDeviceImp());*/
        mWifiSnCount++;
        mWifiSnDataList.remove(0);
    };

    private final Runnable mWriteWifiPwdRunnable = () -> {
        if (mBleBean == null) {
            Timber.e("mWriteWifiPwdRunnable mBleBean == null");
            return;
        }
        if (mBleBean.getOKBLEDeviceImp() == null) {
            Timber.e("mWriteWifiPwdRunnable mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] data = mWifiPwdDataList.get(0);
        Timber.d("mWriteWifiPwdRunnable data %1s", ConvertUtils.bytes2HexString(data));
        LockMessage lockMessage = new LockMessage();
         lockMessage.setBytes(BleCommandFactory.sendSSIDPwdCommand((byte) mWifiPwdLen, (byte) mWifiPwdCount, data));
        lockMessage.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
        lockMessage.setMessageType(3);
        lockMessage.setBleChr(1);
        EventBus.getDefault().post(lockMessage);
       /* 替换
        App.getInstance().writePairMsg(BleCommandFactory.sendSSIDPwdCommand((byte) mWifiPwdLen, (byte) mWifiPwdCount, data), mBleBean.getOKBLEDeviceImp());*/
        mWifiPwdCount++;
        mWifiPwdDataList.remove(0);
    };

    private void writeWifiSn() {
        if (mWifiSnDataList.isEmpty()) {
            if (isStartSend) {
                changeValue(25);
                writeWifiPwd();
            }
            return;
        }
        mHandler.postDelayed(mWriteWifiSnRunnable, 20);
    }

    private void writeWifiPwd() {
        if (mWifiPwdDataList.isEmpty()) {
            isStartSend = false;
            changeValue(50);
            return;
        }
        mHandler.postDelayed(mWriteWifiPwdRunnable, 20);
    }
}
