package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.SettingDuressPwdReceiveEMailBeanReq;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.SettingDuressPwdReceiveEMailBeanRsp;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.DuressParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrDuressRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.AddWifiFailActivity;
import com.revolo.lock.ui.device.add.AddWifiSucActivity;
import com.revolo.lock.ui.device.add.WifiConnectActivity;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.ble.BleProtocolState.CMD_DURESS_PWD_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 胁迫密码
 */
public class DuressCodeActivity extends BaseActivity {
    // TODO: 2021/2/22 所有发送指令都要做超时

    private ConstraintLayout mClInputEmail;
    private ImageView mIvDuressCodeEnable;
    private BleDeviceLocal mBleDeviceLocal;

    private EditText mEtEmail;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_duress_code;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_duress_password));
        mClInputEmail = findViewById(R.id.clInputEmail);
        mIvDuressCodeEnable = findViewById(R.id.ivDuressCodeEnable);
        mEtEmail = findViewById(R.id.etEmail);
        initLoading("Setting...");
        initUI();
        applyDebouncingClickListener(findViewById(R.id.ivDuressCodeEnable), findViewById(R.id.btnSave));
        onRegisterEventBus();
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
            if (null != lockMessage.getBleResultBea()) {
                processBleResult(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES:
                        //当前分两种
                        processOpenOrCloseDuressPwd((WifiLockSetLockAttrDuressRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES:
                        //当前分两种
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }


    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivDuressCodeEnable) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                publishOpenOrCloseDuressPwd(mBleDeviceLocal.getEsn());
            } else {
                openOrCloseDuressPwd();
            }
            return;
        }
        if (view.getId() == R.id.btnSave) {
            settingDuressReceiveMail();
        }
    }

    private void settingDuressReceiveMail() {
        if (!checkNetConnectFail()) {
            return;
        }
        String mail = mEtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.err_tip_please_input_email);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_mail_address);
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("settingDuressReceiveMail token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("settingDuressReceiveMail uid is empty");
            return;
        }
        String loginMail = App.getInstance().getUser().getMail();
        if (loginMail.equals(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_duress_password_email_alike);
            return;
        }
        SettingDuressPwdReceiveEMailBeanReq req = new SettingDuressPwdReceiveEMailBeanReq();
        req.setDuressEmail(mail);
        BleDeviceLocal bleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (null != bleDeviceLocal) {
            req.setDeviceSN(bleDeviceLocal.getEsn());
        }
        req.setUid(uid);
        // 1手机 2邮箱
        req.setType(2);
        Observable<SettingDuressPwdReceiveEMailBeanRsp> observable = HttpRequest.getInstance().settingDuressPwdReceiveEMail(token, req);
        showLoading();
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<SettingDuressPwdReceiveEMailBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull SettingDuressPwdReceiveEMailBeanRsp settingDuressPwdReceiveEMailBeanRsp) {
                dismissLoading();
                String code = settingDuressPwdReceiveEMailBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("settingDuressReceiveMail code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DuressCodeActivity.this);
                        return;
                    }
                    String msg = settingDuressPwdReceiveEMailBeanRsp.getMsg();
                    if (TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("settingDuressReceiveMail code: %1s, msg: %2s", code, settingDuressPwdReceiveEMailBeanRsp.getMsg());
                    return;
                }
                //  SPUtils.getInstance(REVOLO_SP).put(Constant.DURESS_PWD_RECEIVE, mail);
                mBleDeviceLocal.setDuressEmail(mail);
                App.getInstance().setBleDeviceLocal(mBleDeviceLocal);
                AppDatabase.getInstance(DuressCodeActivity.this).bleDeviceDao().update(mBleDeviceLocal);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_email_suc);
                finish();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    //private Disposable mOpenOrCloseDuressPwdDisposable;

    private void publishOpenOrCloseDuressPwd(String wifiID) {
        /*if(mMQttService == null) {
            Timber.e("publishOpenOrCloseDuressPwd mMQttService == null");
            return;
        }*/
        @LocalState.DuressState int mute = mBleDeviceLocal.isDuress() ? LocalState.DURESS_STATE_CLOSE : LocalState.DURESS_STATE_OPEN;
        showLoading();
        DuressParams duressParams = new DuressParams();
        duressParams.setDuress(mute);

        LockMessage lockMessage = new LockMessage();
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_message_code(MQttConstant.SET_LOCK_ATTR);
        MqttMessage mqttMessage = MqttCommandFactory.setLockAttr(wifiID, duressParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())));
        mqttMessage.setQos(0);
        lockMessage.setMqttMessage(mqttMessage);
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        EventBus.getDefault().post(lockMessage);

       /* toDisposable(mOpenOrCloseDuressPwdDisposable);
        mOpenOrCloseDuressPwdDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, duressParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.SET_LOCK_ATTR))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mOpenOrCloseDuressPwdDisposable);
                    processOpenOrCloseDuressPwd(mqttData);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mOpenOrCloseDuressPwdDisposable);*/
    }

    private void processOpenOrCloseDuressPwd(WifiLockSetLockAttrDuressRspBean bean) {
        /*if(TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishOpenOrCloseDuressPwd mqttData.getFunc() is empty");
            return;
        }
        if(mqttData.getFunc().equals(MQttConstant.SET_LOCK_ATTR)) {*/
        dismissLoading();
          /*  Timber.d("publishOpenOrCloseDuressPwd 设置属性: %1s", mqttData);
            WifiLockSetLockAttrDuressRspBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrDuressRspBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }*/
        if (bean == null) {
            Timber.e("publishOpenOrCloseDuressPwd bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishOpenOrCloseDuressPwd bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishOpenOrCloseDuressPwd code : %1d", bean.getCode());
            return;
        }
        saveDuressToLocal();
        //更改后更新到服务端 设备属性
        updateLockInfoToService();
        initUI();
     /*   }
        Timber.d("publishOpenOrCloseDuressPwd %1s", mqttData.toString());*/
    }

    private void initUI() {
        runOnUiThread(() -> {
            mIvDuressCodeEnable.setImageResource(mBleDeviceLocal.isDuress() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            mClInputEmail.setVisibility(mBleDeviceLocal.isDuress() ? View.VISIBLE : View.GONE);
            mEtEmail.setText(mBleDeviceLocal.getDuressEmail());
        });
    }

    private void openOrCloseDuressPwd() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("openOrCloseDuressPwd bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openOrCloseDuressPwd bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd3 = bleBean.getPwd3();
        if (pwd1 == null) {
            Timber.e("openOrCloseDuressPwd pwd1 == null");
            return;
        }
        if (pwd3 == null) {
            Timber.e("openOrCloseDuressPwd pwd3 == null");
            return;
        }
        int control = mBleDeviceLocal.isDuress() ? BleCommandState.DURESS_PWD_CLOSE : BleCommandState.DURESS_PWD_OPEN;
        LockMessage message = new LockMessage();
        message.setBytes(BleCommandFactory.duressPwdSwitch(control, pwd1, pwd3));
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setMessageType(3);
        EventBus.getDefault().post(message);
        // App.getInstance().writeControlMsg(BleCommandFactory.duressPwdSwitch(control, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == CMD_DURESS_PWD_SWITCH) {
            processDuress(bean);
        }
    }

    private void processDuress(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if (state == 0x00) {
            saveDuressToLocal();
            //更改后更新到服务端 设备属性
            updateLockInfoToService();
            initUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
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
        req.setDuress(mBleDeviceLocal.isDuress() ? 0 : 1);
        req.setDoorSensor(mBleDeviceLocal.getDoorSensor());
        req.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 0 : 1);
        req.setAutoLockTime(mBleDeviceLocal.getSetAutoLockTime());
        req.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
        req.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());

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
                    if (!TextUtils.isEmpty(msg))
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);

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

    private void saveDuressToLocal() {
        mBleDeviceLocal.setDuress(!mBleDeviceLocal.isDuress());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

}
