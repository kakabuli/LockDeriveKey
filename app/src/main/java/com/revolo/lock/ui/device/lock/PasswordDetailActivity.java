package com.revolo.lock.ui.device.lock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockRemovePasswordResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.util.ZoneUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_OPTION_DEL;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码详情页面
 */
public class PasswordDetailActivity extends BaseActivity {

    private TextView mTvPwdName, mTvPwd, mTvPwdCharacteristic, mTvCreationDate;
    private ImageView ivEditPwdName;
    private ConstraintLayout tvTipCreationDatePwdView;
    private DevicePwdBean mDevicePwdBean;
    private String mESN;
    private BleDeviceLocal mBleDeviceLocal;

    private static final int REQUEST_CODE = 0xf01;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.PWD_DETAIL)) {
            mDevicePwdBean = intent.getParcelableExtra(Constant.PWD_DETAIL);
        }
        if (mDevicePwdBean == null) {
            finish();
        }
        if (intent.hasExtra(Constant.LOCK_ESN)) {
            mESN = intent.getStringExtra(Constant.LOCK_ESN);
            Timber.d("initData Device Esn: %1s", mESN);
        }
        if (TextUtils.isEmpty(mESN)) {
            // TODO: 2021/2/24 无法获取esn来处理问题
            finish();
        }
        mBleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(mDevicePwdBean.getDeviceId());
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_pwd_details));
        ivEditPwdName = findViewById(R.id.ivEditPwdName);
        tvTipCreationDatePwdView = findViewById(R.id.tvTipCreationDate_pwd_view);
        applyDebouncingClickListener(ivEditPwdName, findViewById(R.id.btnDeletePwd));
        if (null != mBleDeviceLocal && mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            tvTipCreationDatePwdView.setVisibility(View.GONE);
            ivEditPwdName.setVisibility(View.GONE);
        } else {
            tvTipCreationDatePwdView.setVisibility(View.VISIBLE);
            ivEditPwdName.setVisibility(View.VISIBLE);
        }
        mTvPwdName = findViewById(R.id.tvPwdName);
        mTvPwd = findViewById(R.id.tvPwd);
        mTvCreationDate = findViewById(R.id.tvCreationDate);
        mTvPwdCharacteristic = findViewById(R.id.tvPwdCharacteristic);
        //   initZeroTimeZoneDate();
        initSucMessageDialog();
        initFailMessageDialog();
        initLoading(getString(R.string.t_load_content_deleting));
        onRegisterEventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (null == tvTipCreationDatePwdView || null == ivEditPwdName) {
            return;
        }
        if (null != mBleDeviceLocal && mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            tvTipCreationDatePwdView.setVisibility(View.GONE);
            ivEditPwdName.setVisibility(View.GONE);
        } else {
            tvTipCreationDatePwdView.setVisibility(View.VISIBLE);
            ivEditPwdName.setVisibility(View.VISIBLE);
        }
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
                if (lockMessage.getBleResultBea().getCMD() == CMD_KEY_ATTRIBUTES_SET) {
                    delServiceAndLocalKey(lockMessage.getBleResultBea());
                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD:
                        processDelPwd((WifiLockRemovePasswordResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        initDetail();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivEditPwdName) {
            Intent intent = new Intent(this, ChangePwdNameActivity.class);
            intent.putExtra(Constant.PWD_DETAIL, mDevicePwdBean);
            intent.putExtra(Constant.LOCK_ESN, mESN);
            startActivityForResult(intent, REQUEST_CODE);
            return;
        }
        if (view.getId() == R.id.btnDeletePwd) {
            showDelDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String passwordName = data.getStringExtra("passwordName");
            mTvPwdName.setText(passwordName);
        }
    }

    @Override
    protected void onDestroy() {
        mSucMessageDialog = null;
        mFailMessageDialog = null;
        super.onDestroy();
    }

    @SuppressLint("DefaultLocale")
    private void initDetail() {
        if (mDevicePwdBean != null) {
            String pwdName = mDevicePwdBean.getPwdName();
            if (TextUtils.isEmpty(pwdName) || pwdName.equals(mDevicePwdBean.getPwdNum() + "")) {
                pwdName = String.format(getString(R.string.tip_pwd_default_name), mDevicePwdBean.getPwdNum() + "");
            }
            mTvPwdName.setText(pwdName);
            mTvPwd.setText("***********");
            mTvPwdCharacteristic.setText(getPwdCharacteristic(mDevicePwdBean));
            mTvCreationDate.setText(ZoneUtil.getTestDate(mBleDeviceLocal.getTimeZone(),
                    mDevicePwdBean.getCreateTime() * 1000, "MM,dd,yyyy   HH:mm:ss"));
            //mTvCreationDate.setText(ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(),mDevicePwdBean.getCreateTime() * 1000, "MM,dd,yyyy HH:mm:ss"));
        }
    }

    private String getPwdCharacteristic(DevicePwdBean devicePwdBean) {
        int attribute = devicePwdBean.getAttribute();
        String detail = "";
        if (attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
            detail = "Permanent";
        } else if (attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
            long startTimeMill = devicePwdBean.getStartTime() * 1000;
            long endTimeMill = devicePwdBean.getEndTime() * 1000;
            detail = ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), startTimeMill, "MM,dd,yyyy   HH:mm")
                    + "-" + ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), endTimeMill, "MM,dd,yyyy   HH:mm");
        } else if (attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            byte[] weekBytes = BleByteUtil.byteToBit(devicePwdBean.getWeekly());
            String weekly = "";
            if (weekBytes[0] == 0x01) {
                weekly += "Sun";
            }
            if (weekBytes[1] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Mon" : "、Mon";
            }
            if (weekBytes[2] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Tues" : "、Tues";
            }
            if (weekBytes[3] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Wed" : "、Wed";
            }
            if (weekBytes[4] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Thur" : "、Thur";
            }
            if (weekBytes[5] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Fri" : "、Fri";
            }
            if (weekBytes[6] == 0x01) {
                weekly += TextUtils.isEmpty(weekly) ? "Sat" : "、Sat";
            }
            weekly += "\n";
            long startTimeMill = devicePwdBean.getStartTime() * 1000;
            long endTimeMill = devicePwdBean.getEndTime() * 1000;
            detail = weekly
                    + ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), startTimeMill, "HH:mm")
                    + " - "
                    + ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), endTimeMill, "HH:mm");
        }
        return detail;
    }

    private void showDelDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_please_approach_the_door_lock_to_delete_password));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            delPwd();
        });
        dialog.show();
    }

    //private Disposable mDelPwdDisposable;

    private void publishDelPwd(String wifiId, int num) {
       /* if (mMQttService == null) {
            Timber.e("publishDelPwd mMQttService == null");
            return;
        }*/
        LockMessage message = new LockMessage();
        message.setMessageType(2);
        message.setMqtt_message_code(MQttConstant.REMOVE_PWD);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(
                MqttCommandFactory.removePwd(
                        wifiId,
                        0,
                        num,
                        BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(message);
      /*  toDisposable(mDelPwdDisposable);
        mDelPwdDisposable = mMQttService
                .mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                        MqttCommandFactory.removePwd(
                                wifiId,
                                0,
                                num,
                                BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.REMOVE_PWD))
                .subscribe(this::processDelPwd, e -> {
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mDelPwdDisposable);*/
    }

    private void processDelPwd(WifiLockRemovePasswordResponseBean bean) {
        // toDisposable(mDelPwdDisposable);
        /*if (TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }
        */
        //if (mqttData.getFunc().equals(MQttConstant.REMOVE_PWD)) {
        dismissLoading();
        //  Timber.d("删除密码信息: %1s", mqttData);
            /*WifiLockRemovePasswordResponseBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockRemovePasswordResponseBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }*/
        if (bean == null) {
            Timber.e("publishDelPwd bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishDelPwd bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishDelPwd code : %1d", bean.getCode());
            return;
        }
        delKeyFromService();
        //}
        //Timber.d("publishDelPwd %1s", mqttData.toString());
    }

    private void delPwd() {
        showLoading();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishDelPwd(mBleDeviceLocal.getEsn(), mDevicePwdBean.getPwdNum());
        } else {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean == null || bleBean.getOKBLEDeviceImp() == null || bleBean.getPwd1() == null || bleBean.getPwd3() == null) {
                Timber.e("delPwd bleBean == null");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Delete failed, Bluetooth connection failed");
                return;
            }
            LockMessage message = new LockMessage();
            message.setMessageType(3);
            message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
            message.setBytes(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) mDevicePwdBean.getPwdNum(),
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            bleBean.getPwd1(),
                            bleBean.getPwd3()));
            EventBus.getDefault().post(message);
           /* App.getInstance().writeControlMsg(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) mDevicePwdBean.getPwdNum(),
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            (byte)0x00,
                            (byte)0x00,
                            (byte)0x00,
                            bleBean.getPwd1(),
                            bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
        }
    }

    private void delServiceAndLocalKey(BleResultBean bleResultBean) {
        runOnUiThread(() -> {
            if (bleResultBean.getPayload()[0] == 0x00) {
                // 锁端删除成功，执行服务器和本地数据库删除
                delKeyFromService();
            } else {
                // 锁端删除失败
                dismissLoading();
                showFailMessage();
            }
        });
    }

    private void dismissLoadingAndShowFailMessage() {
        dismissLoading();
        showFailMessage();
    }

    private void dismissLoadingAndShowSucMessage() {
        dismissLoading();
        showSucMessage();
    }

    private MessageDialog mFailMessageDialog;
    private MessageDialog mSucMessageDialog;

    private void initSucMessageDialog() {
        mSucMessageDialog = new MessageDialog(PasswordDetailActivity.this);
        mSucMessageDialog.setMessage(getString(R.string.dialog_tip_password_deleted));
        mSucMessageDialog.setOnListener(v -> {
            if (mSucMessageDialog != null) {
                mSucMessageDialog.dismiss();
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 50);
            }
        });
    }

    private void showSucMessage() {
        runOnUiThread(() -> {
            if (mSucMessageDialog != null) {
                mSucMessageDialog.show();
            }
        });

    }

    private void initFailMessageDialog() {
        mFailMessageDialog = new MessageDialog(this);
        mFailMessageDialog.setMessage(getString(R.string.dialog_tip_deletion_failed_door_lock_bluetooth_is_not_found));
        mFailMessageDialog.setOnListener(v -> {
            if (mFailMessageDialog != null) {
                mFailMessageDialog.dismiss();
            }
        });
    }

    private void showFailMessage() {
        runOnUiThread(() -> {
            if (mFailMessageDialog != null) {
                mFailMessageDialog.show();
            }
        });

    }

    private void delKeyFromService() {
        if (!checkNetConnectFail()) {
            return;
        }
        List<DelKeyBeanReq.PwdListBean> listBeans = new ArrayList<>();
        DelKeyBeanReq.PwdListBean pwdListBean = new DelKeyBeanReq.PwdListBean();
        pwdListBean.setNum(mDevicePwdBean.getPwdNum());
        pwdListBean.setPwdType(1);
        listBeans.add(pwdListBean);
        // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除，下面所有
        BleDeviceLocal bleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(mDevicePwdBean.getDeviceId());
        if (bleDeviceLocal == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService bleDeviceLocal == null");
            return;
        }
        String esn = bleDeviceLocal.getEsn();
        if (TextUtils.isEmpty(esn)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService esn is Empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService uid is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService token is Empty");
            return;
        }
        showLoading();
        DelKeyBeanReq req = new DelKeyBeanReq();
        req.setPwdList(listBeans);
        req.setSn(esn);
        req.setUid(uid);
        Observable<DelKeyBeanRsp> observable = HttpRequest.getInstance().delKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelKeyBeanRsp delKeyBeanRsp) {
                String code = delKeyBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    dismissLoadingAndShowFailMessage();
                    Timber.e("delKeyFromService delKeyBeanRsp.getCode() is Empty");
                    return;
                }
                if (!code.equals("200")) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    if (code.equals("444")) {
                        dismissLoading();
                        App.getInstance().logout(true, PasswordDetailActivity.this);
                        return;
                    }
                    dismissLoadingAndShowFailMessage();
                    String msg = delKeyBeanRsp.getMsg();
                    Timber.e("delKeyFromService code: %1s msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                dismissLoadingAndShowSucMessage();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoadingAndShowFailMessage();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

}
