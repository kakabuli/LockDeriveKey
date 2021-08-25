package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SignalWeakDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.DeviceSettingActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

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
    private MessageDialog mMessageDialog;
    private ImageView mIvBatteryState;
    private TextView mTvBatteryState;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            // TODO: 2021/3/1 处理
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (null != mBleDeviceLocal && null != mBleDeviceLocal.getName() && !"".equals(mBleDeviceLocal.getName()) && !mBleDeviceLocal.getEsn().equals(mBleDeviceLocal.getName())) {
            useCommonTitleBar(mBleDeviceLocal.getName());
        } else {
            useCommonTitleBar("Homepage");
        }
        updateView();
        if (mBleDeviceLocal == null) {
            // TODO: 2021/3/1 处理
            finish();
        }
    }

    @Override
    public void notifyNetWork(boolean pingResult) {
        super.notifyNetWork(pingResult);
        updateView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        // TODO: 2021/4/6 抽离文字
        initView();
        BleDeviceLocal mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (null != mBleDeviceLocal && null != mBleDeviceLocal.getName() && !"".equals(mBleDeviceLocal.getName()) && !mBleDeviceLocal.getEsn().equals(mBleDeviceLocal.getName())) {
            useCommonTitleBar(mBleDeviceLocal.getName());
        } else {
            useCommonTitleBar("Homepage");
        }
        initSignalWeakDialog();
        onRegisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_DEVICE_STATE:
                    case LockMessageCode.MSG_LOCK_MESSAGE_UPDATE_BLEDEVICELOCAL:
                        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                        updateView();
                        break;
                }
            } else {
                //数据异常
            }

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                processBleResult(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN:
                        Timber.e("MSG_LOCK_MESSAGE_WF_EVEN curr0 sn:%s", App.getInstance().getmCurrSn());
                        Timber.e("MSG_LOCK_MESSAGE_WF_EVEN Esn:%s", mBleDeviceLocal.getEsn());
                        if (App.getInstance().getmCurrSn().equals(mBleDeviceLocal.getEsn())) {
//                            dismissLoading();
                            //操作
                            mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                            Timber.e("MSG_LOCK_MESSAGE_WF_EVEN curr sn:%s", App.getInstance().getmCurrSn());
                            if (null != mBleDeviceLocal) {
                                updateView();
                            }
                            // processRecord((WifiLockOperationEventBean) lockMessage.getWifiLockBaseResponseBean());
                        }
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
                        //开关锁锁
                        Timber.e("MSG_LOCK_MESSAGE_SET_LOCK curr0 sn:%s", App.getInstance().getmCurrSn());
                        Timber.e("MSG_LOCK_MESSAGE_SET_LOCK Esn:%s", mBleDeviceLocal.getEsn());
                        if (App.getInstance().getmCurrSn().equals(mBleDeviceLocal.getEsn())) {
                            Timber.e("MSG_LOCK_MESSAGE_SET_LOCK curr sn:%s", App.getInstance().getmCurrSn());
//                            dismissLoading();
                            updateView();
                            //processSetLock((WifiLockDoorOptResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        }
                        //   processSetLock((WifiLockDoorOptResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;

                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_WF_EVEN:
                        //操作
                        dismissLoading();
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
                        //开关锁锁
                        dismissLoading();
                        if (mCount == 3) {
                            // 3次机会,超时失败开始连接蓝牙
                            mCount = 0;
                            runOnUiThread(() -> {
                                if (mMessageDialog != null) {
                                    mMessageDialog.show();
                                }
                            });
                        }
                        break;
                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        if (mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean != null && null != bleBean.getOKBLEDeviceImp()) {
                LockMessage message = new LockMessage();
                message.setMessageType(3);
                message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
                message.setBytes(BleCommandFactory
                        .checkLockBaseInfoCommand(bleBean.getPwd1(), bleBean.getPwd3()));
                EventBus.getDefault().post(message);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBleDeviceLocal)
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    ImageView ivLockState, ivNetState, ivDoorState;
    TextView tvNetState, tvDoorState, tvPrivateMode;
    LinearLayout llLowBattery, llNotification, llPwd, llUser, llSetting, llDoorState;

    private void initView() {

        ivLockState = findViewById(R.id.ivLockState);
        ivNetState = findViewById(R.id.ivNetState);
        ivDoorState = findViewById(R.id.ivDoorState);
        tvNetState = findViewById(R.id.tvNetState);
        tvDoorState = findViewById(R.id.tvDoorState);
        llLowBattery = findViewById(R.id.llLowBattery);
        llNotification = findViewById(R.id.llNotification);
        llPwd = findViewById(R.id.llPwd);
        llUser = findViewById(R.id.llUser);
        llSetting = findViewById(R.id.llSetting);
        llDoorState = findViewById(R.id.llDoorState);
        tvPrivateMode = findViewById(R.id.tvPrivateMode);
        mIvBatteryState = findViewById(R.id.ivBatteryState);
        mTvBatteryState = findViewById(R.id.tvBatteryState);
        applyDebouncingClickListener(llNotification, llPwd, llUser, llSetting, ivLockState);
        // updateView();
    }

    /**
     * 更新ui 参数显示
     */
    private void updateView() {
        if (mBleDeviceLocal == null) {
            return;
        }
        // 低电量
        mIvBatteryState.setImageResource(mBleDeviceLocal.getLockPower() <= 20 ? R.drawable.ic_icon_low_battery : R.mipmap.ic_icon_battery);
        mTvBatteryState.setVisibility(mBleDeviceLocal.getLockPower() <= 20 ? View.VISIBLE : View.GONE);
//        llLowBattery.setVisibility(mBleDeviceLocal.getLockPower() <= 20 ? View.VISIBLE : View.GONE);
        if (mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            doorShow(ivDoorState, tvDoorState);
            ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_privacymode));
            tvPrivateMode.setVisibility(View.GONE);
            llDoorState.setVisibility(View.GONE);
        } else {
            tvPrivateMode.setVisibility(View.GONE);
            llDoorState.setVisibility(View.VISIBLE);
            boolean isUseDoorSensor = mBleDeviceLocal.isOpenDoorSensor();
            Timber.d("door sensor state: %1d, isUseDoorSensor: %2b", mBleDeviceLocal.getDoorSensor(), isUseDoorSensor);
            if (mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                ivLockState.setImageResource(R.drawable.ic_home_img_lock_open);
                if (isUseDoorSensor) {
                    doorShow(ivDoorState, tvDoorState);
                    switch (mBleDeviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            doorError(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            // 因为异常，所以与锁的状态同步
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorHide(ivDoorState, tvDoorState);
//                    doorOpen(ivDoorState, tvDoorState);
                }

            } else if (mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE || mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_SENSOR_CLOSE) {
                ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_close));
                if (isUseDoorSensor) {
                    doorShow(ivDoorState, tvDoorState);
                    switch (mBleDeviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            doorError(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_CLOSE:
                            // 因为异常，所以与锁的状态同步
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorHide(ivDoorState, tvDoorState);
//                    doorClose(ivDoorState, tvDoorState);
                }
            } else {
                ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_close));
                // TODO: 2021/3/31 其他选择
                Timber.e("其他选择");
                doorClose(ivDoorState, tvDoorState);
            }
        }
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_wifi);
            ivNetState.setVisibility(View.VISIBLE);
            tvNetState.setVisibility(View.VISIBLE);
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_bluetooth);
            ivNetState.setVisibility(View.VISIBLE);
            tvNetState.setVisibility(View.VISIBLE);
        } else {
            ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_offline));
            tvPrivateMode.setVisibility(View.VISIBLE);
            tvPrivateMode.setText(getString(R.string.t_text_content_offline));
            llDoorState.setVisibility(View.GONE);
            ivNetState.setVisibility(View.GONE);
            tvNetState.setVisibility(View.GONE);
        }
        if (mBleDeviceLocal.getShareUserType() == 1) { // family
            llUser.setVisibility(View.GONE);
        } else if (mBleDeviceLocal.getShareUserType() == 2) { // guest
            llUser.setVisibility(View.GONE);
            llPwd.setVisibility(View.GONE);
            llNotification.setVisibility(View.GONE);
        }
        tvNetState.setText(getString(R.string.tip_online));
        dismissLoading();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.llNotification) {
            Intent intent = new Intent(this, OperationRecordsActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.llPwd) {
            Intent intent = new Intent(this, PasswordListActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.llUser) {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.llSetting) {
            gotoDeviceSettingAct();
            return;
        }
        if (view.getId() == R.id.ivLockState) {
            openDoor();
        }
    }

    private void gotoDeviceSettingAct() {
        if (App.getInstance().getUserBean() == null) {
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

    private void processBleResult(BleResultBean bean) {
        //得判断当前的设备
        Timber.e("processBleResult curr0 mac:%s", App.getInstance().getmCurrMac());
        Timber.e("processBleResult  bean mac:%s", bean.getmMac());
        if (App.getInstance().getmCurrMac().equals(bean.getmMac())) {
            if (bean.getCMD() == BleProtocolState.CMD_LOCK_INFO) {
                dismissLoading();
                mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                if (null != mBleDeviceLocal) {
                    updateView();
                }
            } else if (bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
                dismissLoading();
                mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
                if (null != mBleDeviceLocal) {
                    updateView();
                }
            } else if (bean.getCMD() == BleProtocolState.CMD_LOCK_ALARM_UPLOAD) {
                dismissLoading();
                //报警消息
            }
        }
    }

    private void doorClose(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_closed);
        tvDoorState.setTextColor(getColor(R.color.c666666));
    }

    private void doorError(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_error);
        tvDoorState.setTextColor(getColor(R.color.cFF6A36));
    }

    private void doorOpen(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
        tvDoorState.setText(R.string.tip_opened);
        tvDoorState.setTextColor(getColor(R.color.c666666));
    }

    private void doorHide(ImageView ivDoorState, TextView tvDoorState) {
        llDoorState.setVisibility(View.GONE);
        ivDoorState.setVisibility(View.GONE);
        tvDoorState.setVisibility(View.GONE);
    }

    private void doorShow(ImageView ivDoorState, TextView tvDoorState) {
        llDoorState.setVisibility(View.VISIBLE);
        ivDoorState.setVisibility(View.VISIBLE);
        tvDoorState.setVisibility(View.VISIBLE);
    }

    private void openDoor() {
        @LocalState.LockState int state = mBleDeviceLocal.getLockState();
        if (state == LocalState.LOCK_STATE_PRIVATE) {
            return;
        }
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_DIS) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_text_content_offline_devices);
            return;
        }
        int doorOpt = state == LocalState.LOCK_STATE_OPEN ? LocalState.DOOR_STATE_CLOSE : LocalState.DOOR_STATE_OPEN;
        if (doorOpt == LocalState.DOOR_STATE_OPEN) {
            showLoading("Lock Opening...");
        } else if (doorOpt == LocalState.DOOR_STATE_CLOSE) {
            showLoading("Lock Closing...");
        }
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean == null || bleBean.getOKBLEDeviceImp() == null || bleBean.getPwd1() == null || bleBean.getPwd3() == null) {
                Timber.e("openDoor bleBean == null");
                // TODO 如果双模式蓝牙异常走wifi开门
                if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
                    publishOpenOrCloseLock(
                            mBleDeviceLocal.getEsn(),
                            state == LocalState.LOCK_STATE_OPEN ? LocalState.DOOR_STATE_CLOSE : LocalState.DOOR_STATE_OPEN,
                            mBleDeviceLocal.getRandomCode(), 0);
                }
                return;
            }
            LockMessage message = new LockMessage();
            message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
            message.setBytes(BleCommandFactory
                    .lockControlCommand((byte) (mBleDeviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN ? LOCK_SETTING_CLOSE : LOCK_SETTING_OPEN),
                            (byte) 0x04, (byte) 0x01, bleBean.getPwd1(), bleBean.getPwd3()));
            message.setMessageType(3);
            EventBus.getDefault().post(message);

        } else {
            // TODO: 2021/4/1 主编号是0，分享用户再使用分享用户的编号
            publishOpenOrCloseLock(
                    mBleDeviceLocal.getEsn(),
                    state == LocalState.LOCK_STATE_OPEN ? LocalState.DOOR_STATE_CLOSE : LocalState.DOOR_STATE_OPEN,
                    mBleDeviceLocal.getRandomCode(), 0);
        }
    }

    /**
     * 开关锁
     *
     * @param wifiId  wifi的id
     * @param doorOpt 1:表示开锁，0表示关锁
     */
    public void publishOpenOrCloseLock(String wifiId, @LocalState.DoorState int doorOpt, String randomCode, int num) {
        if (App.getInstance().getUserBean() == null) {
            Timber.e("publishOpenOrCloseDoor App.getInstance().getUserBean() == null");
            return;
        }
        mCount++;
        LockMessage message = new LockMessage();
        message.setMessageType(2);
        message.setMqtt_message_code(MQttConstant.SET_LOCK);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.setLock(wifiId,
                doorOpt,
                BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())),
                randomCode,
                num));
        EventBus.getDefault().post(message);
    }



    /*-------------------------- 多次失败，弹出UI连接蓝牙 --------------------------*/

    private int mCount = 0;

    private void initSignalWeakDialog() {
        mMessageDialog = new MessageDialog(this);
        mMessageDialog.setMessage("Unlock failed.\n Please try it again.");
        mMessageDialog.setOnListener(v -> {
            if (mMessageDialog != null) {
                mMessageDialog.dismiss();
                // connectBle();
            }
        });
    }

    private boolean isRestartConnectingBle = false;
    private BleBean mBleBean;

    private void connectBle() {
        if (mBleDeviceLocal == null) {
            return;
        }
        showLoading("Loading...");
        isRestartConnectingBle = true;
        mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (mBleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if (bleScanResult != null) {
//                mBleBean = App.getInstance().connectDevice(
//                        bleScanResult,
//                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
//                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
//                        mOnBleDeviceListener, false);
//                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            }
//            else {
//                // TODO: 2021/1/26 处理为空的情况
//            }
        } else {
            if (mBleBean.getOKBLEDeviceImp() != null) {
                // mBleBean.setOnBleDeviceListener(mOnBleDeviceListener);
                if (!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
                mBleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                mBleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            }
//            else {
//                // TODO: 2021/1/26 为空的处理
//            }
        }
        // 1分钟后判断设备是否连接成功，否就恢复wifi状态，每秒判断一次是否配对设备成功
        mCountDownTimer.start();
    }

    private final CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mBleBean != null) {
                if (!isRestartConnectingBle) {
                    dismissLoading();
                    mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_BLE);
                    AppDatabase.getInstance(DeviceDetailActivity.this).bleDeviceDao().update(mBleDeviceLocal);
                    updateView();
                    mCountDownTimer.cancel();
                }
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            dismissLoading();
            if (mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }
    };
}
