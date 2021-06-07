package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
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

    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvWifiEnable;
    private boolean isWifiConnected = false;
    private TextView mTvWifiName;

    private SelectDialog mSelectDialog;
    private MessageDialog mPowerLowDialog;
    private ConstraintLayout mCltip;

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
        initLoading("Setting...");
        applyDebouncingClickListener(mIvWifiEnable, findViewById(R.id.tvSettingTitle), findViewById(R.id.clTip));

        mPowerLowDialog = new MessageDialog(this);
        mPowerLowDialog.setMessage(getString(R.string.t_open_wifi_tip_low_power));
        mPowerLowDialog.setOnListener(v -> {
            if (mPowerLowDialog != null) {
                mPowerLowDialog.dismiss();
            }
        });

        mSelectDialog = new SelectDialog(this);
        mSelectDialog.setMessage(getString(R.string.t_closed_wifi_connect_msg));
        mSelectDialog.setOnCancelClickListener(v -> {
            if (mSelectDialog != null) {
                mSelectDialog.dismiss();
            }
        });
        mSelectDialog.setOnConfirmListener(v -> {
            closeWifiFromMQtt();
            if (mSelectDialog != null) {
                mSelectDialog.dismiss();
            }
        });
        onRegisterEventBus();
    }

    @Override
    public void doBusiness() {
        updateUI();

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivWifiEnable) {

            if (mBleDeviceLocal.getLockPower() <= 20) {
                // 低电量
                if (mPowerLowDialog != null) {
                    mPowerLowDialog.show();
                }
            } else {
                if (isWifiConnected) {
                    if (mSelectDialog != null) {
                        mSelectDialog.show();
                    }
                } else {
                    String wifiName = mBleDeviceLocal.getConnectedWifiName();
                    if (TextUtils.isEmpty(wifiName)) {
                        gotoAddWifiAct();
                    } else {
                        openWifiFromBle();
                    }
                }
                return;
            }
        }
        if (view.getId() == R.id.clTip || view.getId() == R.id.tvSettingTitle) {
            // TODO: 2021/4/1 先开启蓝牙再跳转
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                openBleFromMQtt();
            } else {
                gotoAddWifiAct();
            }
        }
    }

    private void gotoAddWifiAct() {

        Intent intent = new Intent(this, AddWifiActivity.class);
        startActivity(intent);
    }

    private void updateUI() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            // Wifi
            updateWifiState();
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            // 蓝牙
            updateBleState();
        } else {
            // TODO: 2021/2/26 do something
        }
    }

    // private Disposable mCloseWifiFromMQttDisposable;

    private void closeWifiFromMQtt() {

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

       /**toDisposable(mCloseWifiFromMQttDisposable);
        mCloseWifiFromMQttDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.closeWifi(
                        mBleDeviceLocal.getEsn(),
                        BleCommandFactory
                                .getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.CLOSE_WIFI))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mCloseWifiFromMQttDisposable);
                    processCloseWifiFromMQtt(mqttData);
                }, e -> {
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mCloseWifiFromMQttDisposable);*/
    }

    private void processCloseWifiFromMQtt(WifiLockCloseWifiResponseBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("closeWifiFromMqtt bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("closeWifiFromMqtt bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("closeWifiFromMqtt code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                ToastUtils.showShort(R.string.t_close_wifi_fail);
            }
            return;
        }
        refreshWifiConnectState();
        updateUI();
        // TODO: 2021/3/4 设置成功, 开启蓝牙连接
        connectBle();

    }

    //private Disposable mOpenBleFromMQttDisposable;

    private void openBleFromMQtt() {
     /*   if (mMQttService == null) {
            Timber.e("openBleFromMQtt mMQttService == null");
            return;
        }*/
        showLoading();
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(
                mBleDeviceLocal.getEsn(), 60/*用于临时开启蓝牙，用于使用蓝牙来重新配网*/,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        EventBus.getDefault().post(lockMessage);

    /*    toDisposable(mOpenBleFromMQttDisposable);
        mOpenBleFromMQttDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.approachOpen(
                        mBleDeviceLocal.getEsn(), 60*//*用于临时开启蓝牙，用于使用蓝牙来重新配网*//*,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.APP_ROACH_OPEN))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mOpenBleFromMQttDisposable);
                    processOpenBleFromMQtt(mqttData);
                }, e -> {
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mOpenBleFromMQttDisposable);*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                processBleResult(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CLOSE_WIFI:
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
                        dismissLoading();
                        break;
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismissLoading();
            App.getInstance().setWifiSettingNeedToCloseBle(true);
            gotoAddWifiAct();
        }, 10000);

    }

    private void connectBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        // TODO: 2021/1/26 处理为空的情况
        if (bleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if (bleScanResult != null) {
             /*   bleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
                        onBleDeviceListener, false);*/
                bleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if (bleBean.getOKBLEDeviceImp() != null) {
              /*  bleBean.setOnBleDeviceListener(onBleDeviceListener);
                if (!bleBean.getOKBLEDeviceImp().isConnected()) {
                    bleBean.getOKBLEDeviceImp().connect(true);
                }
                bleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                bleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                bleBean.setEsn(mBleDeviceLocal.getEsn());*/
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }

    }

    private void openWifiFromBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("openWifiFromBle bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openWifiFromBle bleBean.getOKBLEDeviceImp() == null");
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
        if (bleBean == null) {
            Timber.e("closeWifiFromBle bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("closeWifiFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
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
        });
    }

    private void updateBleState() {
        runOnUiThread(() -> {
            mIvWifiEnable.setImageResource(R.drawable.ic_icon_switch_close);
            isWifiConnected = false;
            mCltip.setVisibility(View.GONE);
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
        mBleDeviceLocal.setConnectedType(isWifiConnected ? LocalState.DEVICE_CONNECT_TYPE_WIFI : LocalState.DEVICE_CONNECT_TYPE_BLE);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

}
