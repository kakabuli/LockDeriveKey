package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.DoorSensorCheckActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 */
public class DoorSensorAlignmentActivity extends BaseActivity {

    // TODO: 2021/3/6 进页面先MQTT读取门磁状态
    private ConstraintLayout mClTip;
    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvDoorMagneticEnable;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_magnet_alignment;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_sensor_alignment));
        mClTip = findViewById(R.id.clTip);
        mIvDoorMagneticEnable = findViewById(R.id.ivDoorMagneticEnable);
        applyDebouncingClickListener(mClTip, mIvDoorMagneticEnable);
        refreshDoorMagneticEnableState();
        onRegisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                changedDoorSensorState(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC:
                        processSetMagnetic((WifiLockSetMagneticResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;

                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    private void refreshDoorMagneticEnableState() {
        runOnUiThread(() -> {
            mIvDoorMagneticEnable.setImageResource(
                    mBleDeviceLocal.isOpenDoorSensor() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            mClTip.setVisibility(mBleDeviceLocal.isOpenDoorSensor() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void doBusiness() {
        if (mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            // initBleListener();
            checkDoorSensorState();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.clTip) {
            gotoDoorSensorCheckAct();
            return;
        }
        if (view.getId() == R.id.ivDoorMagneticEnable) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                if (mBleDeviceLocal.isOpenDoorSensor()) {
                    publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
                } else {
                    gotoDoorSensorCheckAct();
                }

            } else {
                if (mBleDeviceLocal.isOpenDoorSensor()) {
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
                } else {
                    gotoDoorSensorCheckAct();
                }
            }
        }
    }

    private void gotoDoorSensorCheckAct() {
        Intent intent = new Intent(this, DoorSensorCheckActivity.class);
        intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, false);
        startActivity(intent);
        finish();
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if (bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        changedDoorSensorState(bleResultBean);
    };

    /*private void initBleListener() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean != null) {
            bleBean.setOnBleDeviceListener(new OnBleDeviceListener() {
                @Override
                public void onConnected(@NotNull String mac) {

                }

                @Override
                public void onDisconnected(@NotNull String mac) {

                }

                @Override
                public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                    if(value == null) {
                        Timber.e("initBleListener value == null");
                        return;
                    }
                    BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                    if(bleBean == null) {
                        Timber.e("initBleListener bleBean == null");
                        return;
                    }
                    if(bleBean.getOKBLEDeviceImp() == null) {
                        Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                        return;
                    }
                    if(bleBean.getPwd1() == null) {
                        Timber.e("initBleListener bleBean.getPwd1() == null");
                        return;
                    }
                    if(bleBean.getPwd3() == null) {
                        Timber.e("initBleListener bleBean.getPwd3() == null");
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

                }

            });
        }
    }
*/
    private void changedDoorSensorState(BleResultBean bleResultBean) {
        if (bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if (bleResultBean.getPayload()[0] == 0x00) {
                saveDoorSensorStateToLocal();
                refreshDoorMagneticEnableState();
            } else {
                // TODO: 2021/3/6 出错的情况
            }
        } else if (bleResultBean.getCMD() == CMD_LOCK_INFO) {
            byte[] state = BleByteUtil.byteToBit(bleResultBean.getPayload()[4]);
            byte doorSensorState = state[3];
            Timber.d("LockState 7~0: %1s", ConvertUtils.bytes2HexString(state));
            // 更新最新的门磁状态
            mBleDeviceLocal.setOpenDoorSensor(doorSensorState == 0x01);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            refreshDoorMagneticEnableState();
        }
    }

    private void saveDoorSensorStateToLocal() {
        mBleDeviceLocal.setOpenDoorSensor(!mBleDeviceLocal.isOpenDoorSensor());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("sendCommand bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("sendCommand bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        if (pwd1 == null) {
            Timber.e("sendCommand pwd1 == null");
            return;
        }
        byte[] pwd3 = bleBean.getPwd3();
        if (pwd3 == null) {
            Timber.e("sendCommand pwd3 == null");
            return;
        }
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(3);
        lockMessage.setBytes(BleCommandFactory.doorCalibration(doorState, pwd1, pwd3));
        lockMessage.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(lockMessage);

        // App.getInstance().writeControlMsg(BleCommandFactory.doorCalibration(doorState, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void checkDoorSensorState() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("checkDoorSensorState bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("checkDoorSensorState bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        if (pwd1 == null) {
            Timber.e("checkDoorSensorState pwd1 == null");
            return;
        }
        byte[] pwd3 = bleBean.getPwd3();
        if (pwd3 == null) {
            Timber.e("checkDoorSensorState pwd3 == null");
            return;
        }
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(3);
        lockMessage.setBytes(BleCommandFactory.checkLockBaseInfoCommand(pwd1, pwd3));
        lockMessage.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(lockMessage);

        //App.getInstance().writeControlMsg(BleCommandFactory.checkLockBaseInfoCommand(pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    //private Disposable mSetMagneticDisposable;

    public void publishSetMagnetic(String wifiID, @BleCommandState.DoorCalibrationState int mode) {
       /* if (mMQttService == null) {
            Timber.e("publishSetMagnetic mMQttService == null");
            return;
        }*/
        showLoading();
        LockMessage message = new LockMessage();
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        message.setMqtt_message_code(MQttConstant.SET_MAGNETIC);
        message.setMessageType(2);
        EventBus.getDefault().post(message);

       /* toDisposable(mSetMagneticDisposable);
        mSetMagneticDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.SET_MAGNETIC))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mSetMagneticDisposable);
                    processSetMagnetic(mqttData);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mSetMagneticDisposable);*/
    }

    private void processSetMagnetic(WifiLockSetMagneticResponseBean bean) {
      /*  if (TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishSetMagnetic mqttData.getFunc() is empty");
            return;
        }
        if (mqttData.getFunc().equals(MQttConstant.SET_MAGNETIC)) {*/
        dismissLoading();
            /*Timber.d("publishSetMagnetic 设置门磁: %1s", mqttData);
            WifiLockSetMagneticResponseBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetMagneticResponseBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }*/
        if (bean == null) {
            Timber.e("publishSetMagnetic bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishSetMagnetic bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishSetMagnetic code : %1d", bean.getCode());
            return;
        }
        saveDoorSensorStateToLocal();
        refreshDoorMagneticEnableState();

      /*  }
        Timber.d("%1s", mqttData.toString());*/
    }

}
