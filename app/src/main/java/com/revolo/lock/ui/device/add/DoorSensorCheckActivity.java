package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;

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
    private SelectDialog isExitDialog;//退出提示框

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
        mBleDeviceLocal.setOpenDoorSensor(false);//进入配置门磁界面，就清理门磁
        updateLockInfoToService();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mDoorState != DOOR_SUC) {
                showExitHintDialog();
                return true;
            }
            stopTime();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 门磁校验未完成退出
     */
    private void showExitHintDialog() {
        if (null == isExitDialog) {
            isExitDialog = new SelectDialog(this);
            isExitDialog.setMessage(getString(R.string.door_sensor_check_exit_text));
            isExitDialog.setOnCancelClickListener(v -> isExitDialog.dismiss());
            isExitDialog.setOnConfirmListener(v -> {
                isExitDialog.dismiss();
                stopTime();
                finish();
            });
        }
        if (!isExitDialog.isShowing()) {
            isExitDialog.show();
        }
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
                        refreshDoorSensor(mCalibrationState, (WifiLockSetMagneticResponseBean) lockMessage.getWifiLockBaseResponseBean());
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
        useCommonTitleBar(getString(R.string.door_sensor_check_activity_title), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDoorState != DOOR_SUC) {
                    showExitHintDialog();
                    return;
                }
                stopTime();
                finish();
            }
        });
        mBtnNext = findViewById(R.id.btnNext);
        mIvDoorState = findViewById(R.id.ivDoorState);
        mTvTip = findViewById(R.id.tvTip);
        mTvSkip = findViewById(R.id.tvSkip);
        mTvStep = findViewById(R.id.step_hint);
        applyDebouncingClickListener(mBtnNext, mTvSkip);

        initLoading(getString(R.string.t_load_content_loading));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTime();
    }

    private static final int MSG_NEXT_CMD_MSG = 2684;
    private static final int MSG_NEXT_CMD_STOP_MSG = 2685;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_NEXT_CMD_MSG) {
                mBtnNext.setText(getString(R.string.next) + " ( " + msg.obj + "s )");
                mBtnNext.setTextColor(getResources().getColor(R.color.c666666));
                mBtnNext.setBackground(getDrawable(R.drawable.door_sensor_next_but_back));
            } else if (msg.what == MSG_NEXT_CMD_STOP_MSG) {
                mBtnNext.setText(getString(R.string.next));
                mBtnNext.setTextColor(getResources().getColor(R.color.c2C68FF));
                mBtnNext.setBackground(getDrawable(R.drawable.shape_btn_bg));
                stopTime();
            }
        }
    };
    private TimeThread timeThread;

    private void startTime() {
        stopTime();
        if (null == timeThread) {
            timeThread = new TimeThread();
        }
        timeThread.start();
    }

    private void stopTime() {
        if (null != timeThread) {
            timeThread.isRun = false;
            timeThread.index = 0;
            timeThread.interrupt();
            timeThread = null;
        }
    }

    private class TimeThread extends Thread {
        private boolean isRun = true;
        private int index = 6;

        @Override
        public void run() {
            while (isRun) {
                if (index > 0) {
                    handler.obtainMessage(MSG_NEXT_CMD_MSG, index).sendToTarget();
                    index--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    isRun = false;
                    handler.sendEmptyMessage(MSG_NEXT_CMD_STOP_MSG);
                }
            }
        }
    }

    @Override
    public void doBusiness() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
        } else {
            /*BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if (bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            }*/
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
            if (getResources().getColor(R.color.c666666) == mBtnNext.getCurrentTextColor()) {
                return;
            }
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
                   /* if (isGoToAddWifi) {
                        gotoAddWifi();
                    } else {
                        mBleDeviceLocal.setOpenDoorSensor(true);
                        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 50);
                    }*/
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
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        Timber.e("当前电量：" + mBleDeviceLocal.getLockPower());
        if (mBleDeviceLocal.getLockPower() <= 20) {
            // 低电量
            finish();
        } else {
//        if (mBleDeviceLocal.getLockPower() <= 20) {
            // 低电量
            /*if (mPowerLowDialog != null) {
                mPowerLowDialog.show();
            }*/
//        } else {
     /*   mBleDeviceLocal.setOpenDoorSensor(true);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(DoorSensorCheckActivity.this, AddWifiActivity.class);
            startActivity(intent);
            finish();
        }, 50);*/
//        }
      /*  mBleDeviceLocal.setOpenDoorSensor(true);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);*/
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(DoorSensorCheckActivity.this, AddWifiActivity.class);
                intent.putExtra(Constant.WIFI_SETTING_TO_ADD_WIFI, true);
                startActivity(intent);
                finish();
            }, 50);
        }
    }

    /**
     * 更新锁服务器存储的数据
     */
    private void updateLockInfoToService() {
        if (null == this) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("updateLockInfoToService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("updateLockInfoToService uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateLockInfoToService token is empty");
            return;
        }
        showLoading();

        UpdateLockInfoReq req = new UpdateLockInfoReq();
        req.setSn(mBleDeviceLocal.getEsn());
        req.setWifiName(mBleDeviceLocal.getConnectedWifiName());
        req.setSafeMode(0);   // 没有使用这个
        req.setLanguage("en"); // 暂时也没使用这个
        req.setVolume(mBleDeviceLocal.isMute() ? 1 : 0);
        req.setAmMode(mBleDeviceLocal.isAutoLock() ? 0 : 1);
        req.setDuress(mBleDeviceLocal.isDuress() ? 1 : 0);
        req.setMagneticStatus(mBleDeviceLocal.getDoorSensor());
        req.setDoorSensor(mBleDeviceLocal.isOpenDoorSensor() ? 1 : 0);
        req.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 1 : 0);
        req.setAutoLockTime(mBleDeviceLocal.getSetAutoLockTime());
        req.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
        req.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
        Timber.e("std48900444:%s", mBleDeviceLocal.toString());
        Timber.e("std489004445:%s", req.toString());
        Observable<UpdateLockInfoRsp> observable = HttpRequest.getInstance().updateLockInfo(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateLockInfoRsp>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull UpdateLockInfoRsp updateLockInfoRsp) {
                dismissLoading();
                String code = updateLockInfoRsp.getCode();
                if (code.equals("200")) {
                    String msg = updateLockInfoRsp.getMsg();
                    Timber.e("updateLockInfoToService code: %1s, msg: %2s", code, msg);
//                    if (!TextUtils.isEmpty(msg)) ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);

                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                dismissLoading();
                Timber.e(e);

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private boolean isOpenAgain = false;

    private void refreshOpenTheDoor() {
        mIvDoorState.setImageResource(R.mipmap.equipment_img_magneticdoor_open);
        mTvTip.setText(getString(R.string.open_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.door_sensor_check_activity_1_hint_text));
        mTvStep.setVisibility(View.VISIBLE);
        mDoorState = isOpenAgain ? DOOR_OPEN_AGAIN : DOOR_OPEN;
        mTvSkip.setVisibility(isOpenAgain ? View.GONE : View.VISIBLE);
        startTime();
    }

    private void refreshCloseTheDoor() {
        mIvDoorState.setImageResource(R.mipmap.equipment_img_magneticdoor_close);
        mTvTip.setText(getString(R.string.close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.door_sensor_check_activity_2_hint_text));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_CLOSE;
        startTime();
    }

    private void refreshHalfTheDoor() {
        mIvDoorState.setImageResource(R.mipmap.equipment_img_magneticdoor_cover_up);
        mTvTip.setText(getString(R.string.half_close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.door_sensor_check_activity_3_hint_text));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_HALF;
        startTime();
    }

    private void refreshDoorSuc() {
        mDoorState = DOOR_SUC;
        Intent intent = new Intent(DoorSensorCheckActivity.this, DoorCheckOkActivity.class);
        intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, isGoToAddWifi);
        startActivity(intent);
        finish();

      /*  mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
        mTvTip.setText(getString(R.string.door_check_suc_tip));
        mBtnNext.setText(isGoToAddWifi ? getString(R.string.connect_wifi) : getString(R.string.complete));
        mTvSkip.setVisibility(View.GONE);
        mTvStep.setText("");
        mTvStep.setVisibility(View.INVISIBLE);*/

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
                    mBleDeviceLocal.setOpenDoorSensor(true);
                    AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                    refreshDoorSuc();
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
        LockMessage message = new LockMessage();
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
        //更新到服务器
        updateLockInfoToService();

    }

    /*--------------------------------- 蓝牙 -----------------------------------*/

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        showLoading();
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
        LockMessage message = new LockMessage();
        message.setMessageType(3);
        message.setBytes(BleCommandFactory
                .doorCalibration(doorState, bleBean.getPwd1(), bleBean.getPwd3()));
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(message);
       /* 替换
        App.getInstance()
                .writeControlMsg(BleCommandFactory
                        .doorCalibration(doorState, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
    }

    private void changedDoor(BleResultBean bleResultBean) {
        dismissLoading();
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
                //更新到服务器
                updateLockInfoToService();
                checkDoorSensorState();
            } else {
                gotoFailAct();
            }
        }
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
        intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, isGoToAddWifi);
        startActivity(intent);
        finish();
    }

}
