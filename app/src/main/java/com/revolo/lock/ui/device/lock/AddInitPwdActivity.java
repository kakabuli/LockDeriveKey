package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.AddPwdFailDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPwdPublishBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdAttrResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;
import static com.revolo.lock.util.ZoneUtil.getCreatePwdTime;

/**
 * 绑定成功后，添加一个永久性密码
 */
public class AddInitPwdActivity extends BaseActivity {

    private boolean isShowPwd = true;
    private EditText mEtPwd;
    private TextView mTvIntroduceTitle, mTvIntroduceContent;
    private BleDeviceLocal mBleDeviceLocal;
    private BleBean mBleBean;

    @IntDef(value = {PERMANENT_STATE, SCHEDULE_STATE, TEMPORARY_STATE})
    private @interface AttributeState {
    }

    private static final int PERMANENT_STATE = 1;
    private static final int SCHEDULE_STATE = 2;
    private static final int TEMPORARY_STATE = 3;
    @AttributeState
    private int mSelectedPwdState = PERMANENT_STATE;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_input_new_pwd;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mEtPwd = findViewById(R.id.etPwd);
        mTvIntroduceTitle = findViewById(R.id.tvTextThree);
        mTvIntroduceContent = findViewById(R.id.tvTextFour);
        applyDebouncingClickListener(findViewById(R.id.btnNext), mTvIntroduceTitle);

        findViewById(R.id.ivEye).setOnClickListener(v -> {
            ImageView ivEye = findViewById(R.id.ivEye);
            ivEye.setImageResource(isShowPwd ? R.drawable.ic_login_icon_display_blue : R.drawable.ic_login_icon_hide_blue);
            mEtPwd.setInputType(isShowPwd ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            mEtPwd.setKeyListener(DigitsKeyListener.getInstance(getString(R.string.digits_input_password)));
            isShowPwd = !isShowPwd;
        });
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
            BleResultBean bleResultBean = lockMessage.getBleResultBea();
            if (bleResultBean == null) {
                Timber.e("mOnReceivedProcess bleResultBean == null");
                dismissLoadingAndShowAddFail();
                return;
            }
            if (bleResultBean.getCMD() == BleProtocolState.CMD_KEY_ADD) {
                addKey(bleResultBean);
            } else if (bleResultBean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
                byte state = bleResultBean.getPayload()[0];
                if (state == 0x00) {
                    savePwdToService(mDevicePwdBean);
                } else {
                    dismissLoadingAndShowAddFail();
                    Timber.e("设置密钥属性失败，state: %1s", BleByteUtil.byteToInt(state));
                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD:
                        processAddPwd((WifiLockAddPwdRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD:
                        setPwdAttrCallback((WifiLockAddPwdAttrResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD:
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    private int mNum;
    private final DevicePwdBean mDevicePwdBean = new DevicePwdBean();

    private void addKey(BleResultBean bleResultBean) {
        // TODO: 2021/2/4 添加的时候需要判断后时间不能少于前时间
        // 添加密钥
        byte state = bleResultBean.getPayload()[0];
        if (state == 0x00) {
            savePwd(bleResultBean);
        } else {
            dismissLoadingAndShowAddFail();
            Timber.e("添加密钥失败，state: %1s", BleByteUtil.byteToInt(state));
        }
    }

    private void savePwd(BleResultBean bleResultBean) {
        mNum = bleResultBean.getPayload()[1];
        mDevicePwdBean.setPwdNum(mNum);
        // 使用秒存储，所以除以1000
        mDevicePwdBean.setCreateTime(getCreatePwdTime(mBleDeviceLocal.getTimeZone()) / 1000);
        mDevicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        mDevicePwdBean.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        if (mSelectedPwdState == PERMANENT_STATE) {
            savePwdToService(mDevicePwdBean);
        }
    }

    private void setPwdAttrCallback(WifiLockAddPwdAttrResponseBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishAddPwdAttr bean == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishAddPwdAttr code : %1d", bean.getCode());
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishAddPwdAttr bean.getParams() == null");
            return;
        }
        savePwdToService(mDevicePwdBean);
    }

    private void processAddPwd(WifiLockAddPwdRspBean bean) {
        // toDisposable(mAddPwdDisposable);
        dismissLoading();
        if (bean == null) {
            Timber.e("processAddPwd bean == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("processAddPwd code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                showAddPwdFail();
                //ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_add_pwd_fail);
            }
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("processAddPwd bean.getParams() == null");
            return;
        }
        mNum = bean.getParams().getKeyNum();
        mDevicePwdBean.setPwdNum(mNum);
        // 使用秒存储，所以除以1000
        mDevicePwdBean.setCreateTime(getCreatePwdTime(mBleDeviceLocal.getTimeZone()) / 1000);
        mDevicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        mDevicePwdBean.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        if (mSelectedPwdState == PERMANENT_STATE) {
            savePwdToService(mDevicePwdBean);
        }
    }

    private AddPwdFailDialog mAddPwdFailDialog;

    private void dismissLoadingAndShowAddFail() {
        dismissLoading();
        runOnUiThread(() -> {
            showAddPwdFail();
            //ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_add_pwd_fail);
        });
    }

    private void showAddPwdFail() {
        if (null == mAddPwdFailDialog) {
            mAddPwdFailDialog = new AddPwdFailDialog(this);
        }
        if (!mAddPwdFailDialog.isShowing()) {
            mAddPwdFailDialog.show();
        }
    }

    // TODO: 2021/2/4 要做后面时间不能超过前面时间的判断和逻辑处理
    /*--------------------------  把密码上传到服务器  ---------------------------*/
    // TODO: 2021/2/24 要做失败重新请求
    private void savePwdToService(DevicePwdBean devicePwdBean) {
        List<LockKeyAddBeanReq.PwdListBean> pwdListBeans = new ArrayList<>();
        LockKeyAddBeanReq.PwdListBean pwdListBean = new LockKeyAddBeanReq.PwdListBean();
        pwdListBean.setNum(devicePwdBean.getPwdNum());
        pwdListBean.setNickName(devicePwdBean.getPwdNum() + "");
        Timber.e("create time:" + devicePwdBean.getCreateTime());
        pwdListBean.setCreateTime(devicePwdBean.getCreateTime());
        pwdListBean.setPwdType(1);
        if (devicePwdBean.getAttribute() == BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS) {
            pwdListBean.setType(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        } else if (devicePwdBean.getAttribute() == KEY_SET_ATTRIBUTE_TIME_KEY) {
            pwdListBean.setType(KEY_SET_ATTRIBUTE_TIME_KEY);
            pwdListBean.setStartTime(devicePwdBean.getStartTime());
            pwdListBean.setEndTime(devicePwdBean.getEndTime());
        }
        pwdListBeans.add(pwdListBean);
        LockKeyAddBeanReq req = new LockKeyAddBeanReq();
        req.setPwdList(pwdListBeans);
        req.setSn(mBleDeviceLocal.getEsn());
        if (App.getInstance().getUserBean() == null) {
            Timber.e("savePwdToService App.getInstance().getUserBean() is null");
            dismissLoadingAndShowAddFail();
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("savePwdToService uid is Empty");
            dismissLoadingAndShowAddFail();
            return;
        }
        req.setUid(uid);

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("savePwdToService token is Empty");
            dismissLoadingAndShowAddFail();
            return;
        }
        //判断是否手机网络断开
        if (!getNetError()) {
            dismissLoading();
            showSucMessage();
            return;
        }
        dataRequestService(devicePwdBean, req, token);
    }

    private void dataRequestService(@NotNull DevicePwdBean devicePwdBean,
                                    @NotNull LockKeyAddBeanReq req,
                                    @NotNull String token) {
        if (!checkNetConnectFail()) {
            return;
        }
        Observable<LockKeyAddBeanRsp> observable = HttpRequest.getInstance().addLockKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LockKeyAddBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LockKeyAddBeanRsp lockKeyAddBeanRsp) {
                // TODO: 2021/2/24 处理异常情况
                String code = lockKeyAddBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("savePwdToService lockKeyAddBeanRsp.getCode() is Empty");
                    dismissLoadingAndShowAddFail();
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, AddInitPwdActivity.this);
                        return;
                    }
                    if (!TextUtils.isEmpty(lockKeyAddBeanRsp.getMsg())) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(lockKeyAddBeanRsp.getMsg());
                    }
                    Timber.e("savePwdToService code: %1s, msg: %2s", lockKeyAddBeanRsp.getCode(), lockKeyAddBeanRsp.getMsg());
                    dismissLoadingAndShowAddFail();
                    return;
                }
                if (lockKeyAddBeanRsp.getData() == null) {
                    Timber.e("savePwdToService lockKeyAddBeanRsp.getData() == null");
                    dismissLoadingAndShowAddFail();
                    return;
                }
                dismissLoading();
                devicePwdBean.setCreateTime(lockKeyAddBeanRsp.getData().getCreateTime());
                showSucMessage();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
                dismissLoading();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showSucMessage() {
        runOnUiThread(() -> {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.dialog_tip_password_added);
            Intent intent = new Intent(this, AddInitPwdNextActivity.class);
            intent.putExtra(Constant.IS_GO_TO_ADD_WIFI, true);
            startActivity(intent);
            finish();

        });
    }

    private void nextStep(String mKey) {
        if (mKey == null) {
            // TODO: 2021/1/29 处理密码为空的情况
            return;
        }
        showLoading();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishAddPwd(mBleDeviceLocal.getEsn(), mKey);
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (mBleBean == null) {
                Timber.e("nextStep mBleBean == null");
                return;
            }
            if (mBleBean.getPwd1() == null) {
                Timber.e("nextStep mBleBean.getPwd1() == null");
                return;
            }
            if (mBleBean.getPwd3() == null) {
                Timber.e("nextStep mBleBean.getPwd3() == null");
                return;
            }
            LockMessage message = new LockMessage();
            message.setMessageType(3);
            message.setBytes(BleCommandFactory.addKey(KEY_SET_KEY_TYPE_PWD,
                    mKey.getBytes(StandardCharsets.UTF_8), mBleBean.getPwd1(), mBleBean.getPwd3()));
            message.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
            EventBus.getDefault().post(message);
        } else {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.t_text_content_offline_devices));
        }
    }

    private void publishAddPwd(String wifiId, String key) {
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("publishAddPwd uid is empty");
            return;
        }
        showLoading();
        WifiLockAddPwdPublishBean.ParamsBean paramsBean = new WifiLockAddPwdPublishBean.ParamsBean();
        paramsBean.setKey(key);
        // TODO: 2021/3/17 后期修改密钥属性
        paramsBean.setKeyType(0);

        LockMessage message = new LockMessage();
        message.setMqtt_message_code(MQttConstant.CREATE_PWD);
        message.setMqtt_topic(MQttConstant.getCallTopic(uid));
        message.setMqttMessage(MqttCommandFactory.addPwd(
                wifiId,
                paramsBean,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())
                )));
        message.setMessageType(2);
        EventBus.getDefault().post(message);
    }

    @Override
    public void doBusiness() {

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
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            String pwd = mEtPwd.getText().toString().trim();
            if (RegexUtils.isMatch("(?:0(?=1)|1(?=2)|2(?=3)|3(?=4)|4(?=5)|5(?=6)|6(?=7)|7(?=8)|8(?=9)){3,10}\\d", pwd)
                    || RegexUtils.isMatch("(?:9(?=8)|8(?=7)|7(?=6)|6(?=5)|5(?=4)|4(?=3)|3(?=2)|2(?=1)|1(?=0)){3,10}\\d", pwd)
                    || RegexUtils.isMatch("([\\d])\\1{2,}", pwd)) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_dont_enter_a_simple_pwd);
                return;
            }
            if (pwd.startsWith("911")) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_password_start_with_911);
                return;
            }
            if (pwd.length() >= 4 && pwd.length() <= 12) {
                mSelectedPwdState = PERMANENT_STATE;
                nextStep(pwd);
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_pwd);
            }
        } else if (view.getId() == R.id.tvTextThree) {
            if (mTvIntroduceContent.getVisibility() == View.INVISIBLE) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_close);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.VISIBLE);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_more_open);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvIntroduceTitle.setCompoundDrawables(null, null, drawable, null);
                mTvIntroduceContent.setVisibility(View.INVISIBLE);
            }
        }
    }
}

