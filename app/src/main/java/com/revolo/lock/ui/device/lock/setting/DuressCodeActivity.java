package com.revolo.lock.ui.device.lock.setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.SettingDuressPwdReceiveEMailBeanReq;
import com.revolo.lock.bean.respone.SettingDuressPwdReceiveEMailBeanRsp;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

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
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
                        //当前分两种
                        processOpenOrCloseDuressPwd((WifiLockSetLockAttrDuressRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
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
            ToastUtils.showShort(R.string.err_tip_please_input_email);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort(R.string.t_please_input_right_mail_address);
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
        SettingDuressPwdReceiveEMailBeanReq req = new SettingDuressPwdReceiveEMailBeanReq();
        req.setDuressEmail(mail);
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
                        ToastUtils.showShort(msg);
                    }
                    Timber.e("settingDuressReceiveMail code: %1s, msg: %2s", code, settingDuressPwdReceiveEMailBeanRsp.getMsg());
                    return;
                }
                ToastUtils.showShort(R.string.t_setting_email_suc);
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
        lockMessage.setMqttMessage(MqttCommandFactory.setLockAttr(wifiID, duressParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));

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
        initUI();
     /*   }
        Timber.d("publishOpenOrCloseDuressPwd %1s", mqttData.toString());*/
    }

    private void initUI() {
        runOnUiThread(() -> {
            mIvDuressCodeEnable.setImageResource(mBleDeviceLocal.isDuress() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            mClInputEmail.setVisibility(mBleDeviceLocal.isDuress() ? View.VISIBLE : View.GONE);
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
            initUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveDuressToLocal() {
        mBleDeviceLocal.setDuress(!mBleDeviceLocal.isDuress());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

}
