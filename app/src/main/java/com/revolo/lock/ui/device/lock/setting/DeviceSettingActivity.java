package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.UnbindLockDialog;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.VolumeParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrVolumeRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;

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

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.UNBIND_REQ)) {
            mReq = intent.getParcelableExtra(Constant.UNBIND_REQ);
        } else {
            // TODO: 2021/2/6 提示没从上一个页面传递数据过来
            finish();
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if(mBleDeviceLocal == null) {
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
                mIvDoNotDisturbModeEnable, findViewById(R.id.ivLockName));
        mIvDoNotDisturbModeEnable.setImageResource(mBleDeviceLocal.isDoNotDisturbMode()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        mIvMuteEnable.setImageResource(mBleDeviceLocal.isMute()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
    }

    @Override
    public void doBusiness() {
        initData();
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
                // TODO: 2021/2/8 查询一下当前设置
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoading();
        super.onDestroy();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvName || view.getId() == R.id.ivLockName) {
            Intent intent = new Intent(this, ChangeLockNameActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clWifi) {
            Intent intent = new Intent(this, WifiSettingActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clAutoLock) {
            Intent intent = new Intent(this, AutoLockActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clPrivateMode) {
            startActivity(new Intent(this, PrivateModeActivity.class));
            return;
        }
        if(view.getId() == R.id.clDuressCode) {
            Intent intent = new Intent(this, DuressCodeActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clDoorLockInformation) {
            Intent intent = new Intent(this, DoorLockInformationActivity.class);
            intent.putExtra(Constant.UNBIND_REQ, mReq);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clGeoFenceLock) {
            if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                Intent intent = new Intent(this, GeoFenceUnlockActivity.class);
                startActivity(intent);
            }
            return;
        }
        if(view.getId() == R.id.clDoorMagneticSwitch) {
            Intent intent = new Intent(this, DoorSensorAlignmentActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clUnbind) {
            showUnbindDialog();
            return;
        }
        if(view.getId() == R.id.clMute) {
            if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                showLoading("Loading...");
                publishSetVolume(mBleDeviceLocal.getEsn(),
                        mBleDeviceLocal.isMute()?LocalState.VOLUME_STATE_OPEN:LocalState.VOLUME_STATE_MUTE);
            } else {
                mute();
            }
            return;
        }
        if(view.getId() == R.id.ivDoNotDisturbModeEnable) {
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
        mIvDoNotDisturbModeEnable.setImageResource(mBleDeviceLocal.isDoNotDisturbMode()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
    }

    private void initData() {
        String name = mBleDeviceLocal.getName();
        name = TextUtils.isEmpty(name)?mBleDeviceLocal.getEsn():name;
        mTvName.setText(TextUtils.isEmpty(name)?"":name);
        String wifiName = mBleDeviceLocal.getConnectedWifiName();
        mTvWifiName.setText(TextUtils.isEmpty(wifiName)?"":wifiName);
    }

    private void mute() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("mute bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("mute bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("mute bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("mute bleBean.getPwd3() == null");
            return;
        }
        // 0x00：Silent Mode静音
        // 0x01：Low Volume低音量
        // 0x02：High Volume高音量
        byte[] value = new byte[1];
        value[0] = (byte) (mBleDeviceLocal.isMute()?LocalState.VOLUME_STATE_OPEN:LocalState.VOLUME_STATE_MUTE);
        App.getInstance().writeControlMsg(BleCommandFactory.lockParameterModificationCommand((byte) 0x02,
                (byte) 0x01, value, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
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
        if(!checkNetConnectFail()) {
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
                if(code == null) {
                    Timber.e("unbindDevice code == null");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, DeviceSettingActivity.this);
                        return;
                    }
                    String msg = deviceUnbindBeanRsp.getMsg();
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    Timber.e("unbindDevice code: %1s, msg: %2s", code, msg);
                    return;
                }
                // TODO: 2021/2/6 抽离文字 和校对
                // TODO: 2021/2/9 清除了本地所有数据
                App.getInstance().getCacheDiskUtils().clear();
                // 如果是蓝牙，断开蓝牙连接
                BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE
                        && bleBean != null
                        && bleBean.getOKBLEDeviceImp() != null
                        && bleBean.getOKBLEDeviceImp().isConnected()) {
                    App.getInstance().removeConnectedBleBeanAndDisconnect(bleBean);
                }
                App.getInstance().removeBleDeviceLocalFromMac(mBleDeviceLocal.getEsn());
                AppDatabase.getInstance(getApplicationContext()).bleDeviceDao().delete(mBleDeviceLocal);
                ToastUtils.showShort("Unbind success");
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

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        // TODO: 2021/2/7 需要初始化设置和设置各种参数的回调
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processMute(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void processMute(BleResultBean bean) {
        if(bean.getPayload()[0] == 0x00) {
            saveMuteStateToLocal(mBleDeviceLocal.isMute()?LocalState.VOLUME_STATE_OPEN:LocalState.VOLUME_STATE_MUTE);
            refreshMuteEnable();
        } else {
            // TODO: 2021/2/7 信息失败了的操作
            ToastUtils.showShort("Setting Mute Fail");
        }
    }

    private void refreshMuteEnable() {
        runOnUiThread(() -> {
            mIvMuteEnable.setImageResource(mBleDeviceLocal.isMute()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
        });
    }

    private void saveMuteStateToLocal(@LocalState.VolumeState int mute) {
        if(mute == LocalState.VOLUME_STATE_OPEN) {
            mBleDeviceLocal.setMute(false);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        } else if(mute == LocalState.VOLUME_STATE_MUTE) {
            mBleDeviceLocal.setMute(true);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
        refreshMuteEnable();
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
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)))*1000;
        Timber.d("CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected(@NotNull String mac) {

        }

        @Override
        public void onDisconnected(@NotNull String mac) {

        }

        @Override
        public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
            if(value == null) {
                Timber.e("initBleListener value == null");
                return;
            }
            if(!mac.equals(mBleDeviceLocal.getMac())) {
                Timber.e("initBleListener mac: %1s, local mac: %2s", mac, mBleDeviceLocal.getMac());
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean == null) {
                Timber.e("initBleListener bleBean == null");
                return;
            }
            if(bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            if(bleBean.getPwd1() == null) {
                Timber.e("initBleListener bleBean.getPwd1() == null");
                return;
            }
            if(bleBean.getPwd3() == null) {
                Timber.e("initBleListener bleBean.getPwd3() == null");
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, bleBean.getPwd1(), bleBean.getPwd3(), bleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }
    };

    private Disposable mSetVolumeDisposable;

    /**
     * 设置是否静音
     * @param mute 0语音模式 1静音模式
     */
    private void publishSetVolume(String wifiID, @LocalState.VolumeState int mute) {
        if(mMQttService == null) {
            Timber.e("publishSetVolume mMQttService == null");
            return;
        }
        VolumeParams volumeParams = new VolumeParams();
        volumeParams.setVolume(mute);
        toDisposable(mSetVolumeDisposable);
        mSetVolumeDisposable = mMQttService.mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, volumeParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR))
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
        mCompositeDisposable.add(mSetVolumeDisposable);
    }

    private void processSetVolume(MqttData mqttData, @LocalState.VolumeState int mute) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishSetVolume mqttData.getFunc() is empty");
            return;
        }
        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR)) {
            dismissLoading();
            Timber.d("设置属性: %1s", mqttData);
            WifiLockSetLockAttrVolumeRspBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrVolumeRspBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }
            if(bean == null) {
                Timber.e("publishSetVolume bean == null");
                return;
            }
            if(bean.getParams() == null) {
                Timber.e("publishSetVolume bean.getParams() == null");
                return;
            }
            if(bean.getCode() != 200) {
                Timber.e("publishSetVolume code : %1d", bean.getCode());
                return;
            }
            saveMuteStateToLocal(mute);
        }
        Timber.d("publishSetVolume %1s", mqttData.toString());
    }


}
