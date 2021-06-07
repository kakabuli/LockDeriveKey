package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_LIST_CHECK;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 * 步骤：关闭门磁->开门->关门->开门->虚掩->开启门磁
 */
public class DoorSensorCheckActivity extends BaseActivity {

    private ImageView mIvDoorState;
    private TextView mTvTip, mTvSkip, mTvStep;
    private Button mBtnNext;
    private BleDeviceLocal mBleDeviceLocal;
    private boolean isGoToAddWifi = true;

    @IntDef(value = {DOOR_OPEN, DOOR_CLOSE, DOOR_HALF, DOOR_SUC, DOOR_FAIL, DOOR_OPEN_AGAIN})
    private @interface DoorState {
    }

    private static final int DOOR_OPEN = 1;
    private static final int DOOR_CLOSE = 2;
    private static final int DOOR_HALF = 3;
    private static final int DOOR_SUC = 4;
    private static final int DOOR_FAIL = 5;
    private static final int DOOR_OPEN_AGAIN = 6;

    @BleCommandState.DoorCalibrationState
    private int mCalibrationState = BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE;

    @DoorState
    private int mDoorState = DOOR_OPEN;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.IS_GO_TO_ADD_WIFI)) {
            isGoToAddWifi = intent.getBooleanExtra(Constant.IS_GO_TO_ADD_WIFI, true);
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
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
            if (null != lockMessage.getBleResultBea()) {
                changedDoor(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC:
                        refreshDoorSensor(mCalibrationState,(WifiLockSetMagneticResponseBean)lockMessage.getWifiLockBaseResponseBean());
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

    @Override
    public int bindLayout() {
        return R.layout.activity_door_sensor_check;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_sensor_alignment));
        mBtnNext = findViewById(R.id.btnNext);
        mIvDoorState = findViewById(R.id.ivDoorState);
        mTvTip = findViewById(R.id.tvTip);
        mTvSkip = findViewById(R.id.tvSkip);
        mTvStep = findViewById(R.id.tvStep);
        applyDebouncingClickListener(mBtnNext, mTvSkip);

        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
        } else {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if (bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            }
            sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
        }
        // 初始化默认第一步执行开门
        isOpenAgain = false;
        refreshOpenTheDoor();
    }

    @Override
    public void onBackPressed() {
        // 步骤：关闭门磁->开门->关门->开门->虚掩->开启门磁
        if (mDoorState == DOOR_OPEN || mDoorState == DOOR_SUC) {
            super.onBackPressed();
        } else {
            if (mDoorState == DOOR_CLOSE) {
                isOpenAgain = false;
                refreshOpenTheDoor();
            } else if (mDoorState == DOOR_OPEN_AGAIN) {
                refreshCloseTheDoor();
            } else if (mDoorState == DOOR_HALF) {
                isOpenAgain = true;
                refreshOpenTheDoor();
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            switch (mDoorState) {
                case DOOR_OPEN:
                case DOOR_OPEN_AGAIN:
                    if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    }
                    break;
                case DOOR_CLOSE:
                    if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    }
                    break;
                case DOOR_HALF:
                    if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_HALF);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_HALF);
                    }
                    break;
                case DOOR_SUC:
                    if (isGoToAddWifi) {
                        gotoAddWifi();
                    } else {
                        mBleDeviceLocal.setOpenDoorSensor(true);
                        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 50);
                    }
                    break;
                case DOOR_FAIL:
                    break;

            }
            return;
        }
        if (view.getId() == R.id.tvSkip) {
            if (isGoToAddWifi) {
                gotoAddWifi();
            } else {
                finish();
            }
        }
    }

    /*------------------------------------ UI -----------------------------------*/

    private void gotoAddWifi() {
        if (mBleDeviceLocal.getLockPower() <= 20) {
            // 低电量
            if (mPowerLowDialog != null) {
                mPowerLowDialog.show();
            }
        } else {
            mBleDeviceLocal.setOpenDoorSensor(true);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(DoorSensorCheckActivity.this, AddWifiActivity.class);
                startActivity(intent);
                finish();
            }, 50);
        }
        mBleDeviceLocal.setOpenDoorSensor(true);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(DoorSensorCheckActivity.this, AddWifiActivity.class);
            startActivity(intent);
            finish();
        }, 50);
    }

    private boolean isOpenAgain = false;

    private void refreshOpenTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_open);
        mTvTip.setText(getString(R.string.open_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.open_door_step_1_3_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mDoorState = isOpenAgain ? DOOR_OPEN_AGAIN : DOOR_OPEN;
        mTvSkip.setVisibility(isOpenAgain ? View.GONE : View.VISIBLE);
    }

    private void refreshCloseTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_close);
        mTvTip.setText(getString(R.string.close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.close_door_step_2_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_CLOSE;
    }

    private void refreshHalfTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_cover_up);
        mTvTip.setText(getString(R.string.half_close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.half_door_step_4_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_HALF;
    }

    private void refreshDoorSuc() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
        mTvTip.setText(getString(R.string.door_check_suc_tip));
        mBtnNext.setText(isGoToAddWifi ? getString(R.string.connect_wifi) : getString(R.string.complete));
        mTvSkip.setVisibility(View.GONE);
        mTvStep.setText("");
        mTvStep.setVisibility(View.INVISIBLE);
        mDoorState = DOOR_SUC;
    }

    private void refreshCurrentUI() {
        runOnUiThread(() -> {
            switch (mDoorState) {
                case DOOR_OPEN:
                    refreshCloseTheDoor();
                    break;
                case DOOR_CLOSE:
                    isOpenAgain = true;
                    refreshOpenTheDoor();
                    break;
                case DOOR_OPEN_AGAIN:
                    refreshHalfTheDoor();
                    break;
                case DOOR_HALF:
                case DOOR_FAIL:
                    break;
                case DOOR_SUC:
                    refreshDoorSuc();
                    mBleDeviceLocal.setOpenDoorSensor(true);
                    AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                    break;
            }
        });
    }

    /*---------------------------------- MQTT ----------------------------------*/

    private int mLastMsgId = -1;
   // private Disposable mSetMagneticDisposable;

    public void publishSetMagnetic(String wifiID, @BleCommandState.DoorCalibrationState int mode) {
        showLoading();
       /* if (mMQttService == null) {
        if (mMQttService == null) {
            Timber.e("publishSetMagnetic mMQttService == null");
            return;
        }*/
        //toDisposable(mSetMagneticDisposable);
        mCalibrationState = mode;

        //替换
        LockMessage message=new LockMessage();
        message.setMqtt_message_code(MQttConstant.MSG_TYPE_REQUEST);
        message.setMqttMessage(MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        message.setMessageType(2);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        EventBus.getDefault().post(message);

      /*  mSetMagneticDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> refreshDoorSensor(mode, mqttData), e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mSetMagneticDisposable);*/
    }

    private void refreshDoorSensor(@BleCommandState.DoorCalibrationState int mode, WifiLockSetMagneticResponseBean bean) {
       // toDisposable(mSetMagneticDisposable);
        dismissLoading();
        Timber.d("publishSetMagnetic 设置门磁: %1s", bean.toString());
        if (bean == null) {
            Timber.e("publishSetMagnetic bean == null");
            return;
        }
        if (bean.getMsgId() == mLastMsgId) {
            Timber.e("publishSetMagnetic 过滤重复数据ID， msgId: %1d, lastMsgId: %2d", bean.getMsgId(), mLastMsgId);
            return;
        }
        mLastMsgId = bean.getMsgId();
        if (bean.getParams() == null) {
            Timber.e("publishSetMagnetic bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishSetMagnetic code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                // 门磁校验失败
                gotoFailAct();
            }
            return;
        }
        saveDoorSensorStateToLocal(mode);
        // 排除掉第一次发送禁用门磁指令的状态反馈
        if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
            return;
        }
        if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_HALF) {
            publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
            return;
        }
        if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_START_SE) {
            mDoorState = DOOR_SUC;
        }
        refreshCurrentUI();
    private void refreshDoorSensor(@BleCommandState.DoorCalibrationState int mode, MqttData mqttData) {
        toDisposable(mSetMagneticDisposable);
        if (TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishSetMagnetic mqttData.getFunc() is empty");
            return;
        }
        if (mqttData.getFunc().equals(MQttConstant.SET_MAGNETIC)) {
            dismissLoading();
            Timber.d("publishSetMagnetic 设置门磁: %1s", mqttData);
            WifiLockSetMagneticResponseBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetMagneticResponseBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }
            if (bean == null) {
                Timber.e("publishSetMagnetic bean == null");
                return;
            }
            if (bean.getMsgId() == mLastMsgId) {
                Timber.e("publishSetMagnetic 过滤重复数据ID， msgId: %1d, lastMsgId: %2d", bean.getMsgId(), mLastMsgId);
                return;
            }
            mLastMsgId = bean.getMsgId();
            if (bean.getParams() == null) {
                Timber.e("publishSetMagnetic bean.getParams() == null");
                return;
            }
            if (bean.getCode() != 200) {
                Timber.e("publishSetMagnetic code : %1d", bean.getCode());
                if (bean.getCode() == 201) {
                    // 门磁校验失败
                    gotoFailAct();
                }
                return;
            }
            saveDoorSensorStateToLocal(mode);
            // 排除掉第一次发送禁用门磁指令的状态反馈
            if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
                return;
            }
            if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_HALF) {
                publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
                return;
            }
            if (bean.getParams().getMode() == BleCommandState.DOOR_CALIBRATION_STATE_START_SE) {
                mDoorState = DOOR_SUC;
            }
            refreshCurrentUI();
        }
        Timber.d("%1s", mqttData.toString());
    }

    /*--------------------------------- 蓝牙 -----------------------------------*/

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        //替换
        //BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("sendCommand bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("sendCommand bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("sendCommand bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("sendCommand bleBean.getPwd3() == null");
            return;
        }
        mCalibrationState = doorState;
        LockMessage message=new LockMessage();
        message.setMessageType(3);
        message.setBytes(BleCommandFactory
                .doorCalibration(doorState, bleBean.getPwd1(), bleBean.getPwd3()));
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
       /* 替换
        App.getInstance()
                .writeControlMsg(BleCommandFactory
                        .doorCalibration(doorState, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
    }

    };

    private void changedDoor(BleResultBean bleResultBean) {
        if (bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if (bleResultBean.getPayload()[0] == 0x00) {
                saveDoorSensorStateToLocal(mCalibrationState);
                // 排除掉第一次发送禁用门磁指令的状态反馈
                if (mCalibrationState == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
                    return;
                }
                if (mCalibrationState == BleCommandState.DOOR_CALIBRATION_STATE_HALF) {
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
                    return;
                }
                if (mCalibrationState == BleCommandState.DOOR_CALIBRATION_STATE_START_SE) {
                    mDoorState = DOOR_SUC;
                }
                refreshCurrentUI();
            } else {
                gotoFailAct();
            }
        }
    }

    private void saveDoorSensorStateToLocal(@BleCommandState.DoorCalibrationState int state) {
        if (state == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
            mBleDeviceLocal.setOpenDoorSensor(false);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        } else if (state == BleCommandState.DOOR_CALIBRATION_STATE_START_SE) {
            mBleDeviceLocal.setOpenDoorSensor(true);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
    }

    private void gotoFailAct() {
        mDoorState = DOOR_FAIL;
        Intent intent = new Intent(this, DoorCheckFailActivity.class);
        startActivity(intent);
        finish();
    }

}
