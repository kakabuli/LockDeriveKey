package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.OpenBleDialog;
import com.revolo.lock.dialog.OpenDoorDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockConnected;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.DoorSensorCheckActivity;
import com.revolo.lock.ui.device.add.DoorSensorStepActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
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
    private static final int MSG_CONNECT_BLE_OUT_TIME = 3684;//连接蓝牙超时
    private static final int MSG_CONNECT_BLE_OK = 3685;//连接蓝牙成功
    private static final int MSG_CLICK_DOOR_STATE = 3698;//点击开关事件
    private static final int MSG_CLICK_DOOR_NEXT = 3699;//点击开关事件
    private int MSG_CONNECT_BLE_TME = 15000;//ble连接时间
    // TODO: 2021/3/6 进页面先MQTT读取门磁状态
    private ConstraintLayout mClTip;
    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvDoorMagneticEnable;
    private TextView mTvIntroduceTitle, mTvIntroduceContent;
    private OpenBleDialog openBleDialog;
    private OpenDoorDialog openDoorDialog;//打开门磁提示对话框
    private SelectDialog nextDoorDialog;//进入门磁配置界面提示框

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
        mTvIntroduceContent = findViewById(R.id.tvIntroduceContent);
        mTvIntroduceTitle = findViewById(R.id.tvIntroduceTitle);
        refreshDoorMagneticEnableState();
        onRegisterEventBus();

        mTvIntroduceTitle.setOnClickListener(v -> {
            if (mTvIntroduceContent.getVisibility() == View.GONE) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_close);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.VISIBLE);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_open);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.GONE);
            }
        });
        //监听蓝牙开启事件
        setOpenBluetoothClick(new checkOpenBluetoothClick() {
            @Override
            public void onOpenBluetooth(int type) {
                if (type == 1) {
                    //操作1
                    handler.sendEmptyMessageDelayed(MSG_CLICK_DOOR_STATE, 15000);
                    handler.removeMessages(MSG_CLICK_DOOR_NEXT);
                } else if (type == 2) {
                    handler.sendEmptyMessageDelayed(MSG_CLICK_DOOR_NEXT, 15000);
                    handler.removeMessages(MSG_CLICK_DOOR_STATE);
                }
                mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                if (null != mBleDeviceLocal) {
                    BleDeviceLocal bleDeviceLocal = App.getInstance().getBleDeviceLocal();
                    if (bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI ||
                            bleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
                        //WiFi模式下，当手机蓝牙开启后，不会主动连接蓝牙，需要手动连接蓝牙
                        Timber.e("WiFi模式下，当手机蓝牙开启后，不会主动连接蓝牙，需要手动连接蓝牙");
                        handler.sendEmptyMessageDelayed(MSG_CONNECT_BLE_OUT_TIME, MSG_CONNECT_BLE_TME);
                        showOpenBleDialog();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LockMessage lockMessage = new LockMessage();
                                lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(
                                        mBleDeviceLocal.getEsn(), 3/*用于临时开启蓝牙，用于使用蓝牙来重新配网*/,
                                        BleCommandFactory.getPwd(
                                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())), 0, 1));
                                lockMessage.setMessageType(2);
                                lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
                                lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
                                EventBus.getDefault().post(lockMessage);
                            }
                        }, 1500);
                    } else {
                        //当前非WiFi模式下，蓝牙会自动连接
                        Timber.e("当前非WiFi模式下，蓝牙会自动连接");
                        handler.sendEmptyMessageDelayed(MSG_CONNECT_BLE_OUT_TIME, MSG_CONNECT_BLE_TME);
                        showOpenBleDialog();
                    }
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_CONNECT_BLE_OUT_TIME) {
                handler.removeMessages(MSG_CLICK_DOOR_STATE);
                handler.removeMessages(MSG_CLICK_DOOR_NEXT);
                dissOpenBleDialog();
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_fail);
            } else if (msg.what == MSG_CONNECT_BLE_OK) {
                if (handler.hasMessages(MSG_CLICK_DOOR_STATE)) {
                    //点击开关事件
                    checkCurrConnectState();
                } else if (handler.hasMessages(MSG_CLICK_DOOR_NEXT)) {
                    //next 点击事件
                    nextClick();
                }
                handler.removeMessages(MSG_CLICK_DOOR_STATE);
                handler.removeMessages(MSG_CLICK_DOOR_NEXT);
            }
        }
    };

    /**
     * 显示蓝牙连接加载对话框
     */
    private void showOpenBleDialog() {
        runOnUiThread(() -> {
            if (null == openBleDialog) {
                openBleDialog = new OpenBleDialog.Builder(DoorSensorAlignmentActivity.this)
                        .setMessage(getString(R.string.bluetooth_connecting_please_wait))
                        .setCancelable(true)
                        .setCancelOutside(false)
                        .create();
            }
            if (!openBleDialog.isShowing()) {
                openBleDialog.show();
            }
        });
    }

    /**
     * 关闭蓝牙连接加载对话框
     */
    private void dissOpenBleDialog() {
        runOnUiThread(() -> {
            if (null != openBleDialog) {
                if (openBleDialog.isShowing()) {
                    openBleDialog.dismiss();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(MSG_CONNECT_BLE_OUT_TIME);
        handler.removeMessages(MSG_CLICK_DOOR_STATE);
        handler.removeMessages(MSG_CLICK_DOOR_NEXT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息

            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //成功
                if (lockMessage.getMessageCode() == LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE) {
                    Timber.d("door setting activity connected");
                    handler.removeMessages(MSG_CONNECT_BLE_OUT_TIME);
                    dissOpenBleDialog();
                    handler.sendEmptyMessage(MSG_CONNECT_BLE_OK);
                } else {
                    if (null != lockMessage.getBleResultBea()) {
                        changedDoorSensorState(lockMessage.getBleResultBea());
                    }
                }
            } else {
                //异常
                switch (lockMessage.getResultCode()) {


                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC:
                        processSetMagnetic((WifiLockSetMagneticResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN:
                        //无感开门
                        processOpenBleFromMQtt((WifiLockApproachOpenResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_MAGNETIC:
                    case LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    /**
     * mqtt 开启蓝牙广播
     */
    private void openBleFromMQtt() {
        //showLoading();
        handler.sendEmptyMessageDelayed(MSG_CONNECT_BLE_OUT_TIME, MSG_CONNECT_BLE_TME);
        showOpenBleDialog();
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(
                mBleDeviceLocal.getEsn(), 3/*用于临时开启蓝牙，用于使用蓝牙来重新配网*/,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())), 0, 1));
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        EventBus.getDefault().post(lockMessage);
    }

    private void processOpenBleFromMQtt(WifiLockApproachOpenResponseBean bean) {
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectBle();
            }
        }, 2000);
    }

    private void connectBle() {
        //去连接蓝牙
        LockConnected bleConnected = new LockConnected();
        bleConnected.setConnectType(LocalState.CONNECT_STATE_MQTT_CONFIG_DOOR);
        bleConnected.setBleDeviceLocal(mBleDeviceLocal);
        EventBus.getDefault().post(bleConnected);
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
        /*if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            // initBleListener();
            checkDoorSensorState();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.e(" onResume checkDoorSensorState()");
        checkDoorSensorState();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.clTip) {
            //检测当前蓝牙是否开启
            if (!checkIsOpenBluetooth(1)) {
                return;
            }
            if (null == nextDoorDialog) {
                nextDoorDialog = new SelectDialog(this);
            }
            nextDoorDialog.setMessage(getString(R.string.door_next_hint_dialog_content_text));
            nextDoorDialog.setOnConfirmListener(v -> {
                if (nextDoorDialog != null) {
                    nextDoorDialog.dismiss();
                }
                handler.removeMessages(MSG_CLICK_DOOR_NEXT);
                handler.sendEmptyMessageDelayed(MSG_CLICK_DOOR_STATE, 15000);
                checkCurrConnectState();
            });
            nextDoorDialog.setOnCancelClickListener(v -> {
                if (nextDoorDialog != null) {
                    nextDoorDialog.dismiss();
                }
            });
            if (!nextDoorDialog.isShowing()) {
                nextDoorDialog.show();
            }
            return;
        }
        if (view.getId() == R.id.ivDoorMagneticEnable) {
            if (!checkIsOpenBluetooth(2)) {
                return;
            }
            String isDoor = SPUtils.getInstance().getString(Constant.IS_OPEN_DOOR);
            if (null == isDoor || "".equals(isDoor)) {
                mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                if (!mBleDeviceLocal.isOpenDoorSensor()) {
                    //当前未打开门磁校验
                    if (null == openDoorDialog) {
                        openDoorDialog = new OpenDoorDialog(this);
                    }
                    openDoorDialog.setmOnClickListener(v -> {
                        if (null != openDoorDialog) openDoorDialog.dismiss();
                        CheckBox checkBox = openDoorDialog.getCheckBox();
                        if (checkBox != null && checkBox.isChecked()) {
                            SPUtils.getInstance().put(Constant.IS_OPEN_DOOR, "true");
                        }
                        handler.removeMessages(MSG_CLICK_DOOR_STATE);
                        handler.sendEmptyMessageDelayed(MSG_CLICK_DOOR_NEXT, 15000);
                        nextClick();
                    });
                    if (!openDoorDialog.isShowing()) {
                        openDoorDialog.show();
                        return;
                    }
                }
            }
            handler.removeMessages(MSG_CLICK_DOOR_STATE);
            handler.sendEmptyMessageDelayed(MSG_CLICK_DOOR_NEXT, 15000);
            nextClick();
        }
    }

    private void nextClick() {
        Timber.e("nextClick()");
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            if (mBleDeviceLocal.isOpenDoorSensor()) {
                publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
            } else {
                checkCurrConnectState();
            }

        } else {
            if (mBleDeviceLocal.isOpenDoorSensor()) {
                sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
            } else {
                checkCurrConnectState();
            }
        }
    }

    /**
     * 检测当前连接模式
     */
    private void checkCurrConnectState() {
        //当前是WiFi——BLe，ble模式直接进入
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE
                || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            gotoDoorSensorCheckAct();
        } else {
            //mqtt发送命令 开启蓝牙
            openBleFromMQtt();
        }
    }

    private void gotoDoorSensorCheckAct() {
        Intent intent = new Intent(this, DoorSensorStepActivity.class);
        intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, false);
        startActivity(intent);
        boolean registered = EventBus.getDefault().isRegistered(this);
        if (registered) {
            EventBus.getDefault().unregister(this);
        }
        finish();
    }

    private void changedDoorSensorState(BleResultBean bleResultBean) {
        if (bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if (bleResultBean.getPayload()[0] == 0x00) {
                saveDoorSensorStateToLocal();
                //更新到服务器
                Timber.e("更新到服务器1");
                updateLockInfoToService();
                refreshDoorMagneticEnableState();
            } else {
                // TODO: 2021/3/6 出错的情况
            }
        } else if (bleResultBean.getCMD() == CMD_LOCK_INFO) {
            byte[] state = BleByteUtil.byteToBit(bleResultBean.getPayload()[4]);
            byte doorSensorState = state[4];
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

    public void publishSetMagnetic(String wifiID, @BleCommandState.DoorCalibrationState int mode) {
        showLoading();
        LockMessage message = new LockMessage();
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        message.setMqtt_message_code(MQttConstant.SET_MAGNETIC);
        message.setMessageType(2);
        EventBus.getDefault().post(message);
    }


    private void processSetMagnetic(WifiLockSetMagneticResponseBean bean) {
        dismissLoading();
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
        //更新到服务器
        Timber.e("更新到服务器2");
        updateLockInfoToService();
        refreshDoorMagneticEnableState();
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
        Timber.e("std442222445:%s", req.toString());
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
//                    if (!TextUtils.isEmpty(msg))
//                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);

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
}
