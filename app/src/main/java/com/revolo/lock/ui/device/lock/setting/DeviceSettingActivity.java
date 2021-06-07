package com.revolo.lock.ui.device.lock.setting;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.AlexaAppUrlAndWebUrlReq;
import com.revolo.lock.bean.request.AlexaSkillEnableReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.AlexaAppUrlAndWebUrlBeanRsp;
import com.revolo.lock.bean.respone.AlexaSkillEnableBeanRsp;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.UnbindLockDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.VolumeParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_DEVICE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_USER;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备设置
 */
public class DeviceSettingActivity extends BaseActivity {

    private TextView mTvName, mTvWifiName;
    private DeviceUnbindBeanReq mReq;
    private ImageView mIvMuteEnable, mIvDoNotDisturbModeEnable;
    private BleDeviceLocal mBleDeviceLocal;
    private @LocalState.VolumeState
    int lockMute;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.UNBIND_REQ)) {
            mReq = intent.getParcelableExtra(Constant.UNBIND_REQ);
        } else {
            // TODO: 2021/2/6 提示没从上一个页面传递数据过来
            finish();
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            // TODO: 2021/2/22 传递数据为空的处理
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_setting));
        mTvName = findViewById(R.id.tvName);
        mTvWifiName = findViewById(R.id.tvWifiName);
        mIvMuteEnable = findViewById(R.id.ivMuteEnable);
        mIvDoNotDisturbModeEnable = findViewById(R.id.ivDoNotDisturbModeEnable);
        applyDebouncingClickListener(mTvName, findViewById(R.id.clAutoLock), findViewById(R.id.clPrivateMode),
                findViewById(R.id.clDuressCode), findViewById(R.id.clDoorLockInformation),
                findViewById(R.id.clGeoFenceLock), findViewById(R.id.clDoorMagneticSwitch),
                findViewById(R.id.clUnbind), findViewById(R.id.clMute), findViewById(R.id.clWifi),
                mIvDoNotDisturbModeEnable, findViewById(R.id.ivLockName), findViewById(R.id.clJoinAlexa));
        mIvDoNotDisturbModeEnable.setImageResource(mBleDeviceLocal.isDoNotDisturbMode() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
        mIvMuteEnable.setImageResource(mBleDeviceLocal.isMute() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
        onRegisterEventBus();
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
                        processSetVolume((WifiLockSetLockAttrVolumeRspBean) lockMessage.getWifiLockBaseResponseBean(), lockMute);
                        break;

                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        initData();
    }

    @Override
    protected void onDestroy() {
        dismissLoading();
        super.onDestroy();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.tvName || view.getId() == R.id.ivLockName) {
            Intent intent = new Intent(this, ChangeLockNameActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clWifi) {
            Intent intent = new Intent(this, WifiSettingActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clAutoLock) {
            Intent intent = new Intent(this, AutoLockActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clPrivateMode) {
            startActivity(new Intent(this, PrivateModeActivity.class));
            return;
        }
        if (view.getId() == R.id.clDuressCode) {
            Intent intent = new Intent(this, DuressCodeActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clDoorLockInformation) {
            Intent intent = new Intent(this, DoorLockInformationActivity.class);
            intent.putExtra(Constant.UNBIND_REQ, mReq);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clJoinAlexa) {
            joinAlexa();
            return;
        }
        if (view.getId() == R.id.clGeoFenceLock) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                Intent intent = new Intent(this, GeoFenceUnlockActivity.class);
                startActivity(intent);
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Please set to WiFi connection mode");
            }
            return;
        }
        if (view.getId() == R.id.clDoorMagneticSwitch) {
            Intent intent = new Intent(this, DoorSensorAlignmentActivity.class);
            startActivity(intent);
            return;
        }
        if (view.getId() == R.id.clUnbind) {
            showUnbindDialog();
            return;
        }
        if (view.getId() == R.id.clMute) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                showLoading("Loading...");
                publishSetVolume(mBleDeviceLocal.getEsn(),
                        mBleDeviceLocal.isMute() ? LocalState.VOLUME_STATE_OPEN : LocalState.VOLUME_STATE_MUTE);
            } else {
                mute();
            }
            return;
        }
        if (view.getId() == R.id.ivDoNotDisturbModeEnable) {
            // TODO: 2021/3/7 后期要全局实现这个通知功能
            openOrCloseNotification();
        }
    }

    @Override
    public Resources getResources() {
        // 更改布局适应
        return AdaptScreenUtils.adaptHeight(super.getResources(), 703);
    }

    private void openOrCloseNotification() {
        mBleDeviceLocal.setDoNotDisturbMode(!mBleDeviceLocal.isDoNotDisturbMode());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        mIvDoNotDisturbModeEnable.setImageResource(mBleDeviceLocal.isDoNotDisturbMode() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
    }

    private void initData() {
        String name = mBleDeviceLocal.getName();
        name = TextUtils.isEmpty(name) ? mBleDeviceLocal.getEsn() : name;
        mTvName.setText(TextUtils.isEmpty(name) ? "" : name);
        String wifiName = mBleDeviceLocal.getConnectedWifiName();
        mTvWifiName.setText(TextUtils.isEmpty(wifiName) ? "" : wifiName);
    }

    private void mute() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        //替换
        //BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("mute bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("mute bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("mute bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("mute bleBean.getPwd3() == null");
            return;
        }
        // 0x00：Silent Mode静音
        // 0x01：Low Volume低音量
        // 0x02：High Volume高音量
        byte[] value = new byte[1];
        value[0] = (byte) (mBleDeviceLocal.isMute() ? LocalState.VOLUME_STATE_OPEN : LocalState.VOLUME_STATE_MUTE);
        LockMessage ms = new LockMessage();
        ms.setMessageType(3);
        ms.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        ms.setBytes(BleCommandFactory.lockParameterModificationCommand((byte) 0x02,
                (byte) 0x01, value, bleBean.getPwd1(), bleBean.getPwd3()));
        EventBus.getDefault().post(ms);
        /*App.getInstance().writeControlMsg(BleCommandFactory.lockParameterModificationCommand((byte) 0x02,
                (byte) 0x01, value, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
    }

    private void showUnbindDialog() {
        UnbindLockDialog dialog = new UnbindLockDialog(this);
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            unbindDevice();
        });
        dialog.show();
    }

    private void unbindDevice() {
        if (!checkNetConnectFail()) {
            return;
        }
        showLoading("Unbinding...");
        Observable<DeviceUnbindBeanRsp> observable = HttpRequest
                .getInstance().unbindDevice(App.getInstance().getUserBean().getToken(), mReq);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DeviceUnbindBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DeviceUnbindBeanRsp deviceUnbindBeanRsp) {
                dismissLoading();
                String code = deviceUnbindBeanRsp.getCode();
                if (code == null) {
                    Timber.e("unbindDevice code == null");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DeviceSettingActivity.this);
                        return;
                    }
                    String msg = deviceUnbindBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    Timber.e("unbindDevice code: %1s, msg: %2s", code, msg);
                    return;
                }
                // 如果是蓝牙，断开蓝牙连接
                //替换
                //BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
                if (null != bleBean) {
                    App.getInstance().removeConnectedBleDisconnect(bleBean);
                }
                if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE
                        && bleBean != null
                        && bleBean.getOKBLEDeviceImp() != null
                        && bleBean.getOKBLEDeviceImp().isConnected()) {

                    /*//替换
                    App.getInstance().removeConnectedBleBeanAndDisconnect(bleBean);*/
                }
                //清理service中的缓存数据
                LockMessage message = new LockMessage();
                message.setMessageType(MSG_LOCK_MESSAGE_USER);
                message.setMessageCode(MSG_LOCK_MESSAGE_REMOVE_DEVICE);
                message.setMac(mBleDeviceLocal.getMac().toUpperCase());
                EventBus.getDefault().post(message);

                App.getInstance().removeConnectedBleDisconnect(mBleDeviceLocal.getMac());
                AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().delete(mBleDeviceLocal);
                ToastUtils.showShort(R.string.t_unbind_success);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ActivityUtils.finishActivity(DeviceDetailActivity.class);
                    finish();
                }, 50);
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

    private void processBleResult(BleResultBean bean) {
        // TODO: 2021/2/7 需要初始化设置和设置各种参数的回调
        if (bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processMute(bean);
        } else if (bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void processMute(BleResultBean bean) {
        if (bean.getPayload()[0] == 0x00) {
            saveMuteStateToLocal(mBleDeviceLocal.isMute() ? LocalState.VOLUME_STATE_OPEN : LocalState.VOLUME_STATE_MUTE);
        } else {
            ToastUtils.showShort(R.string.t_setting_mute_fail);
        }
    }

    private void refreshMuteEnable() {
        runOnUiThread(() -> mIvMuteEnable.setImageResource(mBleDeviceLocal.isMute() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close));
    }

    private void saveMuteStateToLocal(@LocalState.VolumeState int mute) {
        if (mute == LocalState.VOLUME_STATE_OPEN) {
            mBleDeviceLocal.setMute(false);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        } else if (mute == LocalState.VOLUME_STATE_MUTE) {
            mBleDeviceLocal.setMute(true);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
        refreshMuteEnable();
        updateLockInfoToService();
    }

    private void lockUpdateInfo(BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
        int eventType = bean.getPayload()[0];
        int eventSource = bean.getPayload()[1];
        int eventCode = bean.getPayload()[2];
        int userID = bean.getPayload()[3];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 4, time, 0, time.length);
        // TODO: 2021/2/8 要做时间都是ffffffff的处理判断
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time))) * 1000;
        Timber.d("CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);
    }

    // private Disposable mSetVolumeDisposable;

    /**
     * 设置是否静音
     *
     * @param mute 0语音模式 1静音模式
     */
    private void publishSetVolume(String wifiID, @LocalState.VolumeState int mute) {
        /*if (mMQttService == null) {
            Timber.e("publishSetVolume mMQttService == null");
            return;
        }*/
        lockMute = mute;
        VolumeParams volumeParams = new VolumeParams();
        volumeParams.setVolume(mute);
        LockMessage message = new LockMessage();
        message.setMessageType(2);
        message.setMqtt_message_code(MQttConstant.SET_LOCK_ATTR);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.setLockAttr(wifiID, volumeParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(message);
       /* VolumeParams volumeParams = new VolumeParams();
        volumeParams.setVolume(mute);
        toDisposable(mSetVolumeDisposable);
        mSetVolumeDisposable = mMQttService.mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, volumeParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.SET_LOCK_ATTR))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mSetVolumeDisposable);
                    processSetVolume(mqttData, mute);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mSetVolumeDisposable);*/
    }

    private void processSetVolume(WifiLockSetLockAttrVolumeRspBean bean, @LocalState.VolumeState int mute) {
     /*   if (TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishSetVolume mqttData.getFunc() is empty");
            return;
        }
        if (mqttData.getFunc().equals(MQttConstant.SET_LOCK_ATTR)) {*/
        dismissLoading();
           /* Timber.d("设置属性: %1s", mqttData);
            WifiLockSetLockAttrVolumeRspBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrVolumeRspBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }*/
        if (bean == null) {
            Timber.e("publishSetVolume bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishSetVolume bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishSetVolume code : %1d", bean.getCode());
            return;
        }
        saveMuteStateToLocal(mute);
       /* }
        Timber.d("publishSetVolume %1s", mqttData.toString());
    }*/
    }

    /**
     * 更新锁服务器存储的数据
     */
    private void updateLockInfoToService() {
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
                if (!code.equals("200")) {
                    String msg = updateLockInfoRsp.getMsg();
                    Timber.e("updateLockInfoToService code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) ToastUtils.showShort(msg);
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

    private void joinAlexa() {

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("token is empty");
            return;
        }

        String userMail = App.getInstance().getUser().getMail();
        if (TextUtils.isEmpty(userMail)) {
            Timber.e("userMail is empty");
            return;
        }

        AlexaAppUrlAndWebUrlReq urlReq = new AlexaAppUrlAndWebUrlReq();
        urlReq.setType(1);
        urlReq.setUserMail(userMail);
        Observable<AlexaAppUrlAndWebUrlBeanRsp> appUrlAndWebUrl = HttpRequest.getInstance().getAppUrlAndWebUrl(token, urlReq);
        ObservableDecorator.decorate(appUrlAndWebUrl).safeSubscribe(new Observer<AlexaAppUrlAndWebUrlBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull AlexaAppUrlAndWebUrlBeanRsp alexaAppUrlAndWebUrlBeanRsp) {
                if (alexaAppUrlAndWebUrlBeanRsp != null && alexaAppUrlAndWebUrlBeanRsp.getCode().equals("200")) {
                    if (alexaAppUrlAndWebUrlBeanRsp.getData() != null) {
                        AlexaAppUrlAndWebUrlBeanRsp.DataBean data = alexaAppUrlAndWebUrlBeanRsp.getData();
                        String appUrl = data.getAppUrl();
                        String webFallbackUrl = data.getWebFallbackUrl();
                        runOnUiThread(() -> {
                            if (schemeValid(appUrl)) {
                                gotoAlexa(appUrl);
                            } else {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri uri = Uri.parse(webFallbackUrl);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            String authorizationCode = data.getStringExtra("authorizationCode");
            String stringExtra = data.getStringExtra("state");
            skillAlexa(authorizationCode, stringExtra);
        }
    }

    private final static int REQUEST_CODE = 0xf01;

    private void gotoAlexa(String url) {
        Intent action = new Intent(Intent.ACTION_VIEW);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        action.setData(Uri.parse(stringBuilder.toString()));
        startActivityForResult(action, REQUEST_CODE);
    }

    private boolean schemeValid(String url) {
        PackageManager manager = getPackageManager();
        Intent action = new Intent(Intent.ACTION_VIEW);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        Uri parse = Uri.parse(stringBuilder.toString());
        String host = parse.getHost();
        action.setData(Uri.parse(host));
        List<ResolveInfo> resolveInfos = manager.queryIntentActivities(action, PackageManager.GET_RESOLVED_FILTER);
        return resolveInfos != null && resolveInfos.size() > 0;
    }

    private void skillAlexa(String authorizationCode, String state) {

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("token is empty");
            return;
        }

        String userMail = App.getInstance().getUser().getMail();
        if (TextUtils.isEmpty(userMail)) {
            Timber.e("userMail is empty");
            return;
        }

        if (TextUtils.isEmpty(authorizationCode)) {
            Timber.e("authorizationCode is empty");
            return;
        }

        if (TextUtils.isEmpty(state)) {
            Timber.e("authorizationCode is empty");
            return;
        }

        AlexaSkillEnableReq alexaSkillEnableReq = new AlexaSkillEnableReq();
        alexaSkillEnableReq.setAuthorizationCode(authorizationCode);
        alexaSkillEnableReq.setState(state);
        alexaSkillEnableReq.setType(1);
        alexaSkillEnableReq.setUserMail(userMail);
        Observable<AlexaSkillEnableBeanRsp> alexaSkillEnableBeanRspObservable = HttpRequest.getInstance().skillEnable(token, alexaSkillEnableReq);
        ObservableDecorator.decorate(alexaSkillEnableBeanRspObservable).safeSubscribe(new Observer<AlexaSkillEnableBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull AlexaSkillEnableBeanRsp alexaSkillEnableBeanRsp) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Alexa Skill Success!");
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
