package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.CloseWiFiDialog;
import com.revolo.lock.dialog.ConnectWifiLowBatteryDialog;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.OpenBleDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockConnected;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockCloseWifiResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.AddWifiActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.WIFI_CONTROL_OPEN;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : wifi设置
 */
public class WifiSettingActivity extends BaseActivity {
    private static final int MSG_CONNECT_BLE_OUT_TIME = 3684;//连接蓝牙超时
    private static final int MSG_CONNECT_BLE_OK = 3685;//连接蓝牙成功
    private static final int MSG_CLTIPCLICK = 3687;
    private static final int MSG_ONDEBOUNCINGCLICK = 3688;
    private static final int MSG_CLOSE_WIFI_OUT_TIME = 3690;
    private static final int MSG_CLOSE_WIFI = 3689;
    private static final int MSG_MQTT_BLE_CONNECTED = 3691;
    private static final int MSG_BLE_CONNECTED = 3692;
    private int MSG_CONNECT_BLE_TME = 15000;//ble连接时间
    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvWifiEnable;
    private boolean isWifiConnected = false;
    private TextView mTvWifiName;

    //    private SelectDialog mSelectDialog;
    private ConnectWifiLowBatteryDialog mPowerLowDialog;
    private ConstraintLayout mCltip;
    private OpenBleDialog openBleDialog;
    private CloseWiFiDialog closeWiFiDialog;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_wifi_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_wifi_setting));
        mIvWifiEnable = findViewById(R.id.ivWifiEnable);
        mTvWifiName = findViewById(R.id.tvWifiName);
        mCltip = findViewById(R.id.clTip);
        initLoading(getString(R.string.t_load_content_setting));
        applyDebouncingClickListener(mIvWifiEnable, findViewById(R.id.tvSettingTitle), findViewById(R.id.clTip));


        closeWiFiDialog = new CloseWiFiDialog(this);
        closeWiFiDialog.setOnListener(v -> {
            CheckBox checkBox = closeWiFiDialog.getCheckBox();
            if (checkBox != null && checkBox.isChecked()) {
                SPUtils.getInstance().put("isFirstCloseWiFi", true);
            }
            Timber.e("关闭WiFi");
            clearHandlerMsg(MSG_CLOSE_WIFI);
            handler.sendEmptyMessageDelayed(MSG_CLOSE_WIFI, 60000);
            closeWifiFromMQtt();
            if (closeWiFiDialog != null) {
                closeWiFiDialog.dismiss();
            }
        });

//        mSelectDialog = new SelectDialog(this);
//        mSelectDialog.setMessage(getString(R.string.t_closed_wifi_connect_msg));
//        mSelectDialog.setReturn(true);
//        mSelectDialog.setOnCancelClickListener(v -> {
//            if (mSelectDialog != null) {
//                mSelectDialog.dismiss();
//            }
//        });
//        mSelectDialog.setOnConfirmListener(v -> {
//            Timber.e("关闭WiFi");
//            clearHandlerMsg(MSG_CLOSE_WIFI);
//            handler.sendEmptyMessageDelayed(MSG_CLOSE_WIFI, 60000);
//            closeWifiFromMQtt();
//            if (mSelectDialog != null) {
//                mSelectDialog.dismiss();
//            }
//        });
        onRegisterEventBus();
        setOpenBluetoothClick(new checkOpenBluetoothClick() {
            @Override
            public void onOpenBluetooth(int type) {
                if (type == 1) {
                    clearHandlerMsg(MSG_ONDEBOUNCINGCLICK);
                    handler.sendEmptyMessageDelayed(MSG_ONDEBOUNCINGCLICK, 30000);
                } else if (type == 2) {
                    clearHandlerMsg(MSG_CLTIPCLICK);
                    handler.sendEmptyMessageDelayed(MSG_CLTIPCLICK, 30000);
                }
                Timber.e("监听到当前申请开始蓝牙");
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

    @Override
    protected void onResume() {
        super.onResume();
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (null != mBleDeviceLocal) {
            updateUI();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Timber.e("whatdddddddddd:" + msg.what);
            if (msg.what == MSG_CONNECT_BLE_OUT_TIME) {
                dissOpenBleDialog();
                handler.removeMessages(MSG_CLTIPCLICK);
                handler.removeMessages(MSG_ONDEBOUNCINGCLICK);
                handler.removeMessages(MSG_CLOSE_WIFI);
                handler.removeMessages(MSG_MQTT_BLE_CONNECTED);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_fail);
            } else if (msg.what == MSG_CLOSE_WIFI_OUT_TIME) {
                handler.removeMessages(MSG_CLTIPCLICK);
                handler.removeMessages(MSG_ONDEBOUNCINGCLICK);
                handler.removeMessages(MSG_CLOSE_WIFI);
                handler.removeMessages(MSG_MQTT_BLE_CONNECTED);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_fail);
            } else if (msg.what == MSG_CONNECT_BLE_OK) {
                if (handler.hasMessages(MSG_CLTIPCLICK)) {
                    Timber.e("MSG_CLTIPCLICKMSG_CLTIPCLICKMSG_CLTIPCLICKMSG_CLTIPCLICKMSG_CLTIPCLICK");
                    clTipClick();
                } else if (handler.hasMessages(MSG_ONDEBOUNCINGCLICK)) {
                    Timber.e("MSG_ONDEBOUNCINGCLICKMSG_ONDEBOUNCINGCLICKMSG_ONDEBOUNCINGCLICKMSG_ONDEBOUNCINGCLICK");
                    onDebouncingCli();
                } else if (handler.hasMessages(MSG_CLOSE_WIFI)) {
                    Timber.e("MSG_CLOSE_WIFIMSG_CLOSE_WIFIMSG_CLOSE_WIFIMSG_CLOSE_WIFI");
                    Timber.e("执行WiFi标记");
                } else if (handler.hasMessages(MSG_MQTT_BLE_CONNECTED)) {
                    Timber.e("WiFi跳转");
                    Timber.e("nullllllllllllMSG_MQTT_BLE_CONNECTED");
                    gotoAddWifiAct();
                }
                handler.removeMessages(MSG_MQTT_BLE_CONNECTED);
                handler.removeMessages(MSG_CLTIPCLICK);
                handler.removeMessages(MSG_CLOSE_WIFI);
                handler.removeMessages(MSG_ONDEBOUNCINGCLICK);
            }
        }
    };
    private int[] msgWhats = new int[]{MSG_MQTT_BLE_CONNECTED, MSG_CLTIPCLICK, MSG_CLOSE_WIFI, MSG_ONDEBOUNCINGCLICK};

    /**
     * 清理当前msg标记
     *
     * @param what
     */
    private void clearHandlerMsg(int what) {
        Timber.e("添加msg：" + what);
        for (int i = 0; i < msgWhats.length; i++) {
            if (what != msgWhats[i]) {
                Timber.e("清理msg：" + i + msgWhats[i]);
                handler.removeMessages(msgWhats[i]);
            }
        }
    }

    /**
     * 显示蓝牙连接加载对话框
     */
    private void showOpenBleDialog() {
        runOnUiThread(() -> {
            if (null == openBleDialog) {
                openBleDialog = new OpenBleDialog.Builder(WifiSettingActivity.this)
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void doBusiness() {
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(MSG_CONNECT_BLE_OUT_TIME);
        handler.removeMessages(MSG_CLTIPCLICK);
        handler.removeMessages(MSG_ONDEBOUNCINGCLICK);
        handler.removeMessages(MSG_CLOSE_WIFI);
        handler.removeMessages(MSG_CLOSE_WIFI_OUT_TIME);
        handler.removeMessages(MSG_MQTT_BLE_CONNECTED);
        Timber.e("清理WiFi标记");
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivWifiEnable) {
            boolean isFirstCloseWiFi = SPUtils.getInstance().getBoolean("isFirstCloseWiFi");
            //当前WiFi是打开的
            if (isWifiConnected) {
                if (!isFirstCloseWiFi) {
                    if (closeWiFiDialog != null) {
                        closeWiFiDialog.show();
                    }
                } else {
                    Timber.e("关闭WiFi");
                    clearHandlerMsg(MSG_CLOSE_WIFI);
                    handler.sendEmptyMessageDelayed(MSG_CLOSE_WIFI, 60000);
                    closeWifiFromMQtt();
                }
            } else {
                if (mBleDeviceLocal.getLockPower() <= 20) {
                    // 低电量
                    if (null == mPowerLowDialog) {
                        mPowerLowDialog = new ConnectWifiLowBatteryDialog(this);
                        mPowerLowDialog.setConfirmListener(v -> {
                            if (mPowerLowDialog != null) mPowerLowDialog.dismiss();
                        });
                    }
                    if (!mPowerLowDialog.isShowing()) {
                        mPowerLowDialog.show();
                    }
                } else {
                    //检测当前蓝牙是否开启
                    if (!checkIsOpenBluetooth(1)) {

                        return;
                    }
                    onDebouncingCli();
                }
            }
            return;
        }
        if (view.getId() == R.id.clTip || view.getId() == R.id.tvSettingTitle) {
            //更改设备网络配置
            //检测当前蓝牙是否开启
            if (!checkIsOpenBluetooth(2)) {
                return;
            }
            clTipClick();
        }

    }

    private void onDebouncingCli() {
        String wifiName = mBleDeviceLocal.getConnectedWifiName();
        if (TextUtils.isEmpty(wifiName)) {
            gotoAddWifiAct();
        } else {
            openWifiFromBle();
        }
    }

    private void clTipClick() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            //当前是WiFi连接，在WiFi下判断当前蓝牙是否连接
            Timber.e("WiFi 模式下");
            BleBean bean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (null != bean && bean.getBleConning() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
                //当前蓝牙已连接
                gotoAddWifiAct();
                Timber.e("WiFi 模式下 蓝牙已连接");
            } else {
                //当前蓝牙断开状态
                openBleFromMQtt();
                Timber.e("WiFi 模式下 开始蓝牙连接");
            }
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            //当前是ble连接
            Timber.e("ble 模式下 蓝牙模式");
            gotoAddWifiAct();
        } else {
            //设备掉线
            Timber.e("设备断线中");
            clearHandlerMsg(MSG_MQTT_BLE_CONNECTED);
            handler.sendEmptyMessageDelayed(MSG_MQTT_BLE_CONNECTED, 15000);
            checkBleConnected();
        }
    }

    private void gotoAddWifiAct() {
        String trim = mTvWifiName.getText().toString().trim();
        Intent intent = new Intent(this, AddWifiActivity.class);
        if (!TextUtils.isEmpty(trim)) {
            intent.putExtra(Constant.CONNECT_WIFI_NAME, trim);
        }
        intent.putExtra(Constant.WIFI_SETTING_TO_ADD_WIFI, false);
        startActivity(intent);
        dismissLoading();
    }

    private void updateUI() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            // Wifi
            updateWifiState();
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            // 蓝牙
            updateBleState();
        } else {
            // TODO: 2021/2/26 do something
            //掉线
            updateBleState();
        }
    }

    private void closeWifiFromMQtt() {
//        handler.sendEmptyMessageDelayed(MSG_CLOSE_WIFI_OUT_TIME, 4000);
        setLoadingDialog(true);
        showLoading();
        LockMessage message = new LockMessage();
        message.setMessageType(2);
        message.setMqtt_message_code(MQttConstant.CLOSE_WIFI);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.closeWifi(
                mBleDeviceLocal.getEsn(),
                BleCommandFactory
                        .getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(message);
    }

    private void processCloseWifiFromMQtt(WifiLockCloseWifiResponseBean bean) {
        if (bean.getCode() != 200) {
            Timber.e("closeWifiFromMqtt code : %1d", bean.getCode());
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_close_wifi_fail);
            return;
        }
        refreshWifiConnectState();
        updateUI();
        // TODO: 2021/3/4 设置成功, 开启蓝牙连接
        connectBle();

    }

    /**
     * 检查当前蓝牙连接
     */
    private void checkBleConnected() {
        Timber.e("开始蓝牙连接");
        handler.sendEmptyMessageDelayed(MSG_CONNECT_BLE_OUT_TIME, MSG_CONNECT_BLE_TME);
        showOpenBleDialog();
        connectBle();
    }

    private void openBleFromMQtt() {
        //showLoading();
        handler.sendEmptyMessageDelayed(MSG_CONNECT_BLE_OUT_TIME, MSG_CONNECT_BLE_TME);
        clearHandlerMsg(MSG_MQTT_BLE_CONNECTED);
        handler.sendEmptyMessageDelayed(MSG_MQTT_BLE_CONNECTED, 15000);
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            Timber.e("ble 消息 ：%s", lockMessage.toString());
            //蓝牙消息
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //成功
                if (lockMessage.getMessageCode() == LockMessageCode.MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE) {
                    Timber.d("wifi setting activity connected");
                    handler.removeMessages(MSG_CONNECT_BLE_OUT_TIME);
                    dissOpenBleDialog();
                    handler.sendEmptyMessage(MSG_CONNECT_BLE_OK);
                } else {
                    if (null != lockMessage.getBleResultBea()) {
                        processBleResult(lockMessage.getBleResultBea());
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
                    case LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI:
                        handler.removeMessages(MSG_CLOSE_WIFI_OUT_TIME);
                        processCloseWifiFromMQtt((WifiLockCloseWifiResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN:
                        //无感开门
                        processOpenBleFromMQtt((WifiLockApproachOpenResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI:
                    case LockMessageCode.MSG_LOCK_MESSAGE_APP_ROACH_OPEN:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
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
        connectBle();
    }

    private void connectBle() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //去连接蓝牙
                LockConnected bleConnected = new LockConnected();
                bleConnected.setConnectType(LocalState.CONNECT_STATE_MQTT_CONFIG_DOOR);
                bleConnected.setBleDeviceLocal(mBleDeviceLocal);
                EventBus.getDefault().post(bleConnected);
            }
        }, 2000);
    }

    /**
     * 开启WiFi
     */
    private void openWifiFromBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (null == bleBean || null == bleBean.getOKBLEDeviceImp()) {
            Timber.e("closeWifiFromBle bleBean == null");
            clearHandlerMsg(MSG_ONDEBOUNCINGCLICK);
            handler.sendEmptyMessageDelayed(MSG_ONDEBOUNCINGCLICK, 30000);
            checkBleConnected();
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd3 = bleBean.getPwd3();
        if (pwd1 == null) {
            Timber.e("openWifiFromBle pwd1 == null");
            return;
        }
        if (pwd3 == null) {
            Timber.e("openWifiFromBle pwd3 == null");
            return;
        }
        LockMessage message = new LockMessage();
        message.setMessageType(3);
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setBytes(BleCommandFactory.wifiSwitch(WIFI_CONTROL_OPEN, pwd1, pwd3));
        EventBus.getDefault().post(message);
        //App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(WIFI_CONTROL_OPEN, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void closeWifiFromBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (null == bleBean || null == bleBean.getOKBLEDeviceImp()) {
            Timber.e("closeWifiFromBle bleBean == null");
            checkBleConnected();
            return;
        }
      /*  if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("closeWifiFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }*/
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd3 = bleBean.getPwd3();
        if (pwd1 == null) {
            Timber.e("closeWifiFromBle pwd1 == null");
            return;
        }
        if (pwd3 == null) {
            Timber.e("closeWifiFromBle pwd3 == null");
            return;
        }
        LockMessage message = new LockMessage();
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setMessageType(3);
        message.setBytes(BleCommandFactory.wifiSwitch(BleCommandState.WIFI_CONTROL_CLOSE, pwd1, pwd3));
        EventBus.getDefault().post(message);
        //App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(BleCommandState.WIFI_CONTROL_CLOSE, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void updateWifiState() {
        runOnUiThread(() -> {
            mIvWifiEnable.setImageResource(R.drawable.ic_icon_switch_open);
            String wifiName = mBleDeviceLocal.getConnectedWifiName();
            mTvWifiName.setText(TextUtils.isEmpty(wifiName) ? "" : wifiName);
            isWifiConnected = true;
            mCltip.setVisibility(View.VISIBLE);
            // 打开WiFi成功，主动断开蓝牙连接
            App.getInstance().removeConnectedBleDisconnect(mBleDeviceLocal.getMac());
        });
    }

    private void updateBleState() {
        runOnUiThread(() -> {
            mIvWifiEnable.setImageResource(R.drawable.ic_icon_switch_close);
            isWifiConnected = false;
            mCltip.setVisibility(View.INVISIBLE);
        });
    }

    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == CMD_WIFI_SWITCH) {
            processWifiSwitch(bean);
        }
    }

    private void processWifiSwitch(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if (state == 0x00) {
            refreshWifiConnectState();
            updateUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void refreshWifiConnectState() {
        isWifiConnected = !isWifiConnected;
        if (isWifiConnected) {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (null != bleBean && bleBean.getBleConning() == 2) {
                mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE);
            } else {
                mBleDeviceLocal.setConnectedType(LocalState.DEVICE_CONNECT_TYPE_WIFI);
            }
        } else {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            int state = LocalState.DEVICE_CONNECT_TYPE_BLE;
            if (null == bleBean) {
                state = LocalState.DEVICE_CONNECT_TYPE_DIS;
            } else {
                if (bleBean.getBleConning() != 2) {
                    state = LocalState.DEVICE_CONNECT_TYPE_DIS;
                }
            }
            mBleDeviceLocal.setConnectedType(state);
        }
        App.getInstance().setBleDeviceLocal(mBleDeviceLocal);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }
}
