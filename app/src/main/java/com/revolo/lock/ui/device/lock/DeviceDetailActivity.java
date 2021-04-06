package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.SignalWeakDialog;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockDoorOptResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.DeviceSettingActivity;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_CLOSE;
import static com.revolo.lock.ble.BleCommandState.LOCK_SETTING_OPEN;

/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备详情页面
 */
public class DeviceDetailActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private SignalWeakDialog mSignalWeakDialog;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if(mBleDeviceLocal == null) {
            // TODO: 2021/3/1 处理
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        // TODO: 2021/4/6 抽离文字
        useCommonTitleBar("Homepage");
        initSignalWeakDialog();
    }

    @Override
    public void doBusiness() {
        initDevice();
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.llNotification) {
            Intent intent = new Intent(this, OperationRecordsActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llPwd) {
            Intent intent = new Intent(this, PasswordListActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llUser) {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.llSetting) {
            gotoDeviceSettingAct();
            return;
        }
        if(view.getId() == R.id.ivLockState) {
            openDoor();
        }
    }

    private void gotoDeviceSettingAct() {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("gotoDeviceSettingAct App.getInstance().getUserBean() == null");
            return;
        }
        Intent intent = new Intent(this, DeviceSettingActivity.class);
        DeviceUnbindBeanReq req = new DeviceUnbindBeanReq();
        req.setUid(App.getInstance().getUserBean().getUid());
        req.setWifiSN(mBleDeviceLocal.getEsn());
        intent.putExtra(Constant.UNBIND_REQ, req);
        startActivity(intent);
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {

        }

        @Override
        public void onDisconnected(@NotNull String mac) {

        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            if(value == null) {
                Timber.e("mOnBleDeviceListener value == null");
                return;
            }
            if(!mBleDeviceLocal.getMac().equals(mac)) {
                Timber.e("mOnBleDeviceListener mac: %1s, localMac: %2s", mac, mBleDeviceLocal.getMac());
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("mOnBleDeviceListener bleBean == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("mOnBleDeviceListener bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("mOnBleDeviceListener bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
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
            if(mac.equals(mBleDeviceLocal.getMac())) {
                isRestartConnectingBle = false;
            }
        }

    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
            lockInfo(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_CONTROL_ACK) {
            // TODO: 2021/3/31 看是否需要判定锁
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void lockInfo(BleResultBean bean) {
        // TODO: 2021/2/8 锁基本信息处理
        byte[] lockFunBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 0, lockFunBytes, 0, lockFunBytes.length);
        // 以下标来命名区分 bit0~7
        byte[] bit0_7 = BleByteUtil.byteToBit(lockFunBytes[3]);
        // bit8~15
        byte[] bit8_15 = BleByteUtil.byteToBit(lockFunBytes[2]);
        // bit16~23
        byte[] bit16_23 = BleByteUtil.byteToBit(lockFunBytes[1]);

        byte[] lockState = new byte[4];
        System.arraycopy(bean.getPayload(), 4, lockState, 0, lockState.length);
        byte[] lockStateBit0_7 = BleByteUtil.byteToBit(lockState[3]);
        byte[] lockStateBit8_15 = BleByteUtil.byteToBit(lockState[2]);
        int soundVolume = bean.getPayload()[8];
        byte[] language = new byte[2];
        System.arraycopy(bean.getPayload(), 9, language, 0, language.length);
        String languageStr = new String(language, StandardCharsets.UTF_8);
        int battery = bean.getPayload()[11];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 12, time, 0, time.length);
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME)*1000;
        Timber.d("CMD: %1d, lockFunBytes: bit0_7: %2s, bit8_15: %3s, bit16_23: %4s, lockStateBit0_7: %5s, lockStateBit8_15: %6s, soundVolume: %7d, language: %8s, battery: %9d, time: %10d",
                bean.getCMD(), ConvertUtils.bytes2HexString(bit0_7), ConvertUtils.bytes2HexString(bit8_15),
                ConvertUtils.bytes2HexString(bit16_23), ConvertUtils.bytes2HexString(lockStateBit0_7),
                ConvertUtils.bytes2HexString(lockStateBit8_15), soundVolume, languageStr, battery, realTime);

    }

    private void setLockState(@LocalState.LockState int state) {
        mBleDeviceLocal.setLockState(state);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        initDevice();
    }

    private void setDoorState(@LocalState.DoorSensor int door) {
        mBleDeviceLocal.setDoorSensor(door);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        initDevice();
    }

    private void lockUpdateInfo(BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
        int eventType = bean.getPayload()[0];
        int eventSource = bean.getPayload()[1];
        int eventCode = bean.getPayload()[2];
        int userID = bean.getPayload()[3];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 4, time, 0, time.length);
        // TODO: 2021/2/8 要做时间都是ffffffff的处理判断
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)))*1000;
        Timber.d("CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);

        // TODO: 2021/2/10 后期需要移植修改
        runOnUiThread(() -> {
            if(eventType == 0x01) {
                if(eventCode == 0x01) {
                    // 上锁
                    setLockState(LocalState.LOCK_STATE_CLOSE);
                } else if(eventCode == 0x02) {
                    // 开锁
                    setLockState(LocalState.LOCK_STATE_OPEN);
                } else {
                    // TODO: 2021/2/10 其他处理
                }
            } else if(eventType == 0x04) {
                // sensor附加状态，门磁
                if(eventCode == LocalState.DOOR_SENSOR_OPEN) {
                    // 开门
                    setDoorState(LocalState.DOOR_SENSOR_OPEN);
                } else if(eventCode == LocalState.DOOR_SENSOR_CLOSE) {
                    // 关门
                    setDoorState(LocalState.DOOR_SENSOR_CLOSE);
                } else if(eventCode == LocalState.DOOR_SENSOR_EXCEPTION) {
                    // 门磁异常
                    // TODO: 2021/3/31 门磁异常的操作
                    Timber.d("lockUpdateInfo 门磁异常");
                } else {
                    // TODO: 2021/3/31 异常值
                }
            }
        });
    }


    private void initDevice() {
        ImageView ivLockState = findViewById(R.id.ivLockState);
        ImageView ivNetState = findViewById(R.id.ivNetState);
        ImageView ivDoorState = findViewById(R.id.ivDoorState);
        TextView tvNetState = findViewById(R.id.tvNetState);
        TextView tvDoorState = findViewById(R.id.tvDoorState);
        LinearLayout llLowBattery = findViewById(R.id.llLowBattery);
        LinearLayout llNotification = findViewById(R.id.llNotification);
        LinearLayout llPwd = findViewById(R.id.llPwd);
        LinearLayout llUser = findViewById(R.id.llUser);
        LinearLayout llSetting = findViewById(R.id.llSetting);
        LinearLayout llDoorState = findViewById(R.id.llDoorState);
        TextView tvPrivateMode = findViewById(R.id.tvPrivateMode);

        applyDebouncingClickListener(llNotification, llPwd, llUser, llSetting, ivLockState);

        if(mBleDeviceLocal == null) {
            return;
        }
        // 低电量
        llLowBattery.setVisibility(mBleDeviceLocal.getLockPower() <= 20?View.VISIBLE:View.GONE);
        if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_privacymodel));
            tvPrivateMode.setVisibility(View.VISIBLE);
            llDoorState.setVisibility(View.GONE);
        } else {
            tvPrivateMode.setVisibility(View.GONE);
            llDoorState.setVisibility(View.VISIBLE);
            boolean isUseDoorSensor = mBleDeviceLocal.isOpenDoorSensor();
            if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                ivLockState.setImageResource(R.drawable.ic_home_img_lock_open);
                if(isUseDoorSensor) {
                    switch (mBleDeviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                        case LocalState.DOOR_SENSOR_OPEN:
                            // 因为异常，所以与锁的状态同步
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorOpen(ivDoorState, tvDoorState);
                }

            } else if(mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE) {
                ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_close));
                if(isUseDoorSensor) {
                    switch (mBleDeviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            // 因为异常，所以与锁的状态同步
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorClose(ivDoorState, tvDoorState);
                }
            } else {
                // TODO: 2021/3/31 其他选择
                Timber.e("");
            }

        }
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_wifi);
        } else if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_bluetooth);
        } else {
            // TODO: 2021/3/2 其他选择
            Timber.e("");
        }
        tvNetState.setText(getString(R.string.tip_online));

    }

    private void doorClose(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_closed);
    }

    private void doorOpen(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
        tvDoorState.setText(R.string.tip_opened);
    }

    private void openDoor() {
        @LocalState.LockState int state = mBleDeviceLocal.getLockState();
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            // TODO: 2021/4/1 主编号是0，分享用户再使用分享用户的编号
            publishOpenOrCloseLock(
                    mBleDeviceLocal.getEsn(),
                    state==LocalState.LOCK_STATE_OPEN?LocalState.DOOR_STATE_CLOSE:LocalState.DOOR_STATE_OPEN,
                    mBleDeviceLocal.getRandomCode(), 0);
        } else {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("openDoor bleBean == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("openDoor bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
                Timber.e("openDoor bleBean.getPwd3() == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("openDoor bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            App.getInstance().writeControlMsg(BleCommandFactory
                    .lockControlCommand((byte) (mBleDeviceLocal.getLockState()==LocalState.LOCK_STATE_OPEN?LOCK_SETTING_CLOSE:LOCK_SETTING_OPEN),
                            (byte) 0x04, (byte) 0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
        }
    }

    /**
     *  开关锁
     * @param wifiId wifi的id
     * @param doorOpt 1:表示开锁，0表示关锁
     */
    public void publishOpenOrCloseLock(String wifiId, @LocalState.DoorState int doorOpt, String randomCode, int num) {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("publishOpenOrCloseDoor App.getInstance().getUserBean() == null");
            return;
        }
        if(doorOpt == LocalState.DOOR_STATE_OPEN) {
            showLoading("Lock Opening...");
        } else if(doorOpt == LocalState.DOOR_STATE_CLOSE) {
            showLoading("Lock Closing...");
        }
        mCount++;
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLock(wifiId,
                        doorOpt,
                        BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())),
                        randomCode,
                        num))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .safeSubscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                mCount = 0;
                processMQttMsg(mqttData);
            }

            @Override
            public void onError(@NotNull Throwable e) {
                dismissLoading();
                if(e instanceof TimeoutException) {
                    if(mCount == 3) {
                        // 3次机会,超时失败开始连接蓝牙
                        mCount = 0;
                        runOnUiThread(() -> {
                            if(mSignalWeakDialog != null) {
                                mSignalWeakDialog.show();
                            }
                        });

                    }
                }
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void processMQttMsg(@NotNull MqttData mqttData) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        // TODO: 2021/3/3 处理开关锁的回调信息
        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK)) {
            processSetLock(mqttData);
        } else if(mqttData.getFunc().equals(MqttConstant.WF_EVENT)) {
            processRecord(mqttData);
        }
    }

    private void processRecord(@NotNull MqttData mqttData) {
        WifiLockOperationEventBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockOperationEventBean.class);
        } catch (JsonSyntaxException e) {
            Timber.e(e);
            return;
        }
        if(bean == null) {
            Timber.e("processRecord RECORD bean == null");
            return;
        }
        if(bean.getWfId() == null) {
            Timber.e("processRecord RECORD bean.getWfId() == null");
            return;
        }
        if(!bean.getWfId().equals(mBleDeviceLocal.getEsn())) {
            Timber.e("processRecord RECORD wifiId: %1s current esn: %2s",
                    bean.getWfId(), mBleDeviceLocal.getEsn());
            return;
        }
        if(bean.getEventparams() == null) {
            Timber.e("processRecord RECORD bean.getEventparams() == null");
            return;
        }
        if(bean.getEventtype() == null) {
            Timber.e("processRecord RECORD bean.getEventtype() == null");
            return;
        }
        if(!bean.getEventtype().equals(MqttConstant.RECORD)) {
            Timber.e("processRecord RECORD eventType: %1s", bean.getEventtype());
            return;
        }
        if(bean.getEventparams().getEventType() == 1) {
            // 动作操作
            if(bean.getEventparams().getEventCode() == 1) {
                // 上锁
                setLockState(LocalState.LOCK_STATE_CLOSE);
            } else if(bean.getEventparams().getEventCode() == 2) {
                // 开锁
                setLockState(LocalState.LOCK_STATE_OPEN);
            }
        } else if(bean.getEventparams().getEventType() == 4) {
            // 传感器上报，门磁
            if(bean.getEventparams().getEventCode() == 1) {
                // 门磁开门
                setDoorState(LocalState.DOOR_SENSOR_OPEN);
            } else if(bean.getEventparams().getEventCode() == 2) {
                // 门磁关门
                setDoorState(LocalState.DOOR_SENSOR_CLOSE);
            } else if(bean.getEventparams().getEventCode() == 3) {
                // 门磁异常
                Timber.e("processRecord 门磁异常");
            }
        }
    }

    private void processSetLock(@NotNull MqttData mqttData) {
        dismissLoading();
        WifiLockDoorOptResponseBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockDoorOptResponseBean.class);
        } catch (JsonSyntaxException e) {
            Timber.e(e);
            return;
        }
        if(bean == null) {
            Timber.e("processSetLock bean == null");
            return;
        }
        if(bean.getParams() == null) {
            Timber.e("processSetLock bean.getParams() == null");
            return;
        }
        if(bean.getCode() != 200) {
            Timber.e("processSetLock code : %1d", bean.getCode());
        }
    }

    /*-------------------------- 多次失败，弹出UI连接蓝牙 --------------------------*/

    private int mCount = 0;

    private void initSignalWeakDialog() {
        mSignalWeakDialog = new SignalWeakDialog(this);
        mSignalWeakDialog.setOnCancelClickListener(v -> {
            if(mSignalWeakDialog != null) {
                mSignalWeakDialog.dismiss();
            }
        });
        mSignalWeakDialog.setOnConfirmListener(v -> {
            if(mSignalWeakDialog != null) {
                mSignalWeakDialog.dismiss();
            }
            connectBle();
        });
    }

    private boolean isRestartConnectingBle = false;
    private BleBean mBleBean;

    private void connectBle() {
        if(mBleDeviceLocal == null) {
            return;
        }
        showLoading("Loading...");
        isRestartConnectingBle = true;
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(mBleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if(bleScanResult != null) {
                mBleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
                        mOnBleDeviceListener,false);
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if(mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.setOnBleDeviceListener(mOnBleDeviceListener);
                if(!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
                mBleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                mBleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
        // 1分钟后判断设备是否连接成功，否就恢复wifi状态，每秒判断一次是否配对设备成功
        mCountDownTimer.start();
    }

    private final CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(mBleBean != null) {
                if(!isRestartConnectingBle) {
                    dismissLoading();
                    mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
                    AppDatabase.getInstance(DeviceDetailActivity.this).bleDeviceDao().update(mBleDeviceLocal);
                    initDevice();
                    mCountDownTimer.cancel();
                }
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            dismissLoading();
            if(mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }
    };

}
