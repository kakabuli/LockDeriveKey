package com.revolo.lock.ui.device.lock.setting;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AmModeParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AutoLockTimeParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoTimeRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;
import static com.revolo.lock.ble.BleProtocolState.CMD_SET_AUTO_LOCK_TIME;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自动上锁设置
 */
public class AutoLockActivity extends BaseActivity {

    private SeekBar mSeekBar;
    private TextView mTvTime, mTvDetectionLock, mTvTip, mTvIntroduceTitle, mTvIntroduceContent;
    private int mTime = 0;
    private int mBeforeTime = 0;
    private ImageView mIvDetectionLockEnable, mIvAutoLockEnable;
    private ConstraintLayout mClSetLockTime;
    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auto_lock;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        onRegisterEventBus();
        useCommonTitleBar(getString(R.string.title_auto_lock));
        mSeekBar = findViewById(R.id.seekBar);
        mTvTime = findViewById(R.id.tvTime);
        mTvTip = findViewById(R.id.tvTip);
        mTvDetectionLock = findViewById(R.id.tvDetectionLock);
        mClSetLockTime = findViewById(R.id.clSetLockTime);
        mIvAutoLockEnable = findViewById(R.id.ivAutoLockEnable);
        mTvIntroduceTitle = findViewById(R.id.tvIntroduceTitle);
        mTvIntroduceContent = findViewById(R.id.tvIntroduceContent);
        mIvDetectionLockEnable = findViewById(R.id.ivDetectionLockEnable);
        applyDebouncingClickListener(mIvAutoLockEnable, mIvDetectionLockEnable);
        initLoading(getString(R.string.t_load_content_setting));
        mSeekBar.setMax(140);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChange(progress);
                mBeforeTime = mTime;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                stopTrackingTouch(seekBar);
            }
        });
        mTime = mBleDeviceLocal.getSetAutoLockTime();

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
        initUI();
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
                Timber.e("%s", lockMessage.toString());
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO:
                        processOpenOrCloseAutoLock((WifiLockSetLockAttrAutoRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME:
                        processAutoLockTime((WifiLockSetLockAttrAutoTimeRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    // 超时或者其他错误
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO:
                    case LockMessageCode.MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME:
                        dismissLoading();
                        break;
                }
                mSeekBar.setProgress(mBeforeTime);
            }
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivAutoLockEnable) {
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
                publishOpenOrCloseAutoLock(mBleDeviceLocal.getEsn());
            } else {
                openOrCloseAutoLock();
            }
            return;
        }
        if (view.getId() == R.id.ivDetectionLockEnable) {
            openOrCloseDetectionLock();
        }
    }

    private void initUI() {
        runOnUiThread(() -> {
            Timber.d("initUI mTime: %1d, AutoTime: %2d", mTime, mBleDeviceLocal.getSetAutoLockTime());
            mIvAutoLockEnable.setImageResource(mBleDeviceLocal.isAutoLock() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            mTvDetectionLock.setVisibility(mBleDeviceLocal.isAutoLock() ? View.VISIBLE : View.GONE);
            mIvDetectionLockEnable.setVisibility(mBleDeviceLocal.isAutoLock() ? View.VISIBLE : View.GONE);
            mClSetLockTime.setVisibility(mBleDeviceLocal.isAutoLock() ? View.VISIBLE : View.GONE);
            mTvTime.setText(mTime == 0 ? getString(R.string.activity_auto_lock_immediately) : getTimeString(mTime));
            mIvDetectionLockEnable
                    .setImageResource(mBleDeviceLocal.isDetectionLock() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            mSeekBar.setProgress(getProgressFromTime(mBleDeviceLocal.getSetAutoLockTime()));
            mTvTip.setText(getString(mTime == 0 ? R.string.tip_the_timer_will_start_when_your_door_is_closed : R.string.tip_door_will_be_locked_when_time_is_up));
        });

    }

    // private Disposable mOpenOrCloseAutoLockDisposable;

    private void publishOpenOrCloseAutoLock(String wifiID) {
        /*if (mMQttService == null) {
            Timber.e("publishOpenOrCloseAutoLock mMQttService == null");
            return;
        }*/
        // toDisposable(mOpenOrCloseAutoLockDisposable);
        @LocalState.AutoState int auto = mBleDeviceLocal.isAutoLock() ? LocalState.AUTO_STATE_CLOSE : LocalState.AUTO_STATE_OPEN;
        showLoading();
        AmModeParams amModeParams = new AmModeParams();
        amModeParams.setAmMode(auto);
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqtt_message_code(MQttConstant.SET_LOCK_ATTR);
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        lockMessage.setMqttMessage(MqttCommandFactory.setLockAttr(wifiID, amModeParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(lockMessage);
    }

    private void processOpenOrCloseAutoLock(WifiLockSetLockAttrAutoRspBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishOpenOrCloseAutoLock bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishOpenOrCloseAutoLock bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishOpenOrCloseAutoLock code : %1d", bean.getCode());
            return;
        }
        updateLockInfoToService(false);
        initUI();
    }

    private void publishAutoLockTime(String wifiID, int time) {

        showLoading();
        AutoLockTimeParams autoLockTimeParams = new AutoLockTimeParams();
        autoLockTimeParams.setAutoLockTime(time);
        LockMessage message = new LockMessage();
        message.setMqtt_message_code(MQttConstant.SET_LOCK_ATTR);
        message.setMessageType(2);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.setLockAttr(wifiID, autoLockTimeParams,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        EventBus.getDefault().post(message);
    }

    private void processAutoLockTime(WifiLockSetLockAttrAutoTimeRspBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishAutoLockTime bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishAutoLockTime bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishAutoLockTime code : %1d", bean.getCode());
            return;
        }
        updateLockInfoToService(true);
    }

    private void openOrCloseDetectionLock() {
        // TODO: 2021/2/22 服务器开启，或者本地开关 2021/2/22 开启服务通知
        mBleDeviceLocal.setDetectionLock(!mBleDeviceLocal.isDetectionLock());
        initUI();
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    // TODO: 2021/2/8 要接收回调处理
    private void openOrCloseAutoLock() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("openOrCloseAutoLock bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getPwd3() == null");
            return;
        }
        byte[] value = new byte[1];
        value[0] = (byte) (mBleDeviceLocal.isAutoLock() ? 0x01 : 0x00);
        LockMessage message = new LockMessage();
        message.setMessageType(3);
        message.setBytes(BleCommandFactory
                .lockParameterModificationCommand((byte) 0x04, (byte) 0x01, value, bleBean.getPwd1(),
                        bleBean.getPwd3()));
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(message);
    }

    private void stopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress >= 0 && progress < 10) {
            if (!mBleDeviceLocal.isOpenDoorSensor()) {
                mTvTime.setText("10s");
                mTime = 10;
                seekBar.setProgress(20);
            } else {
                mTvTime.setText(getString(R.string.activity_auto_lock_immediately));
                mTime = 0;
            }
        } else if (progress >= 10 && progress < 20) {
            if (!mBleDeviceLocal.isOpenDoorSensor()) {
                mTvTime.setText("10s");
                mTime = 10;
                seekBar.setProgress(20);
            } else {
                mTvTime.setText("5s");
                mTime = 5;
            }
        } else if (progress >= 20 && progress < 30) {
            mTvTime.setText("10s");
            mTime = 10;
        } else if (progress >= 30 && progress < 40) {
            mTvTime.setText("15s");
            mTime = 15;
        } else if (progress >= 40 && progress < 50) {
            mTvTime.setText("20s");
            mTime = 20;
        } else if (progress >= 50 && progress < 60) {
            mTvTime.setText("25s");
            mTime = 25;
        } else if (progress >= 60 && progress < 70) {
            mTvTime.setText("30s");
            mTime = 30;
        } else if (progress >= 70 && progress < 80) {
            mTvTime.setText("1min");
            mTime = 60;
        } else if (progress >= 80 && progress < 90) {
            mTvTime.setText("2min");
            mTime = 2 * 60;
        } else if (progress >= 90 && progress < 100) {
            mTvTime.setText("5min");
            mTime = 5 * 60;
        } else if (progress >= 100 && progress < 110) {
            mTvTime.setText("10min");
            mTime = 10 * 60;
        } else if (progress >= 110 && progress < 120) {
            mTvTime.setText("15min");
            mTime = 15 * 60;
        } else if (progress >= 120 && progress < 130) {
            mTvTime.setText("20min");
            mTime = 20 * 60;
        } else if (progress >= 130 && progress < 140) {
            mTvTime.setText("30min");
            mTime = 30 * 60;
        }
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishAutoLockTime(mBleDeviceLocal.getEsn(), mTime);
        } else {
            setAutoLockTimeFromBle();
        }
    }

    private void setAutoLockTimeFromBle() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("setAutoLockTimeFromBle bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getPwd3() == null");
            return;
        }
        LockMessage message = new LockMessage();
        message.setBytes(BleCommandFactory
                .setAutoLockTime(mTime, bleBean.getPwd1(), bleBean.getPwd3()));
        message.setMessageType(3);
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        EventBus.getDefault().post(message);
    }

    private int getProgressFromTime(int time) {
        switch (time) {
            case 0:
                return 1;
            case 5:
                return 15;
            case 10:
                return 25;
            case 15:
                return 35;
            case 20:
                return 45;
            case 25:
                return 55;
            case 30:
                return 65;
            case 60:
                return 75;
            case 2 * 60:
                return 85;
            case 5 * 60:
                return 95;
            case 10 * 60:
                return 105;
            case 15 * 60:
                return 115;
            case 20 * 60:
                return 125;
            case 30 * 60:
                return 140;
            default:
                return 0;
        }
    }

    private void progressChange(int progress) {
        if (progress >= 0 && progress < 10) {
            if (!mBleDeviceLocal.isOpenDoorSensor()) {
                mTvTime.setText("10s");
                mTime = 10;
                mSeekBar.setProgress(20);
            } else {
                mTvTime.setText(getString(R.string.activity_auto_lock_immediately));
                mTime = 0;
            }
        } else if (progress >= 10 && progress < 20) {
            if (!mBleDeviceLocal.isOpenDoorSensor()) {
                mTvTime.setText("10s");
                mTime = 10;
                mSeekBar.setProgress(20);
            } else {
                mTvTime.setText("5s");
                mTime = 5;
            }
        } else if (progress >= 20 && progress < 30) {
            mTvTime.setText("10s");
        } else if (progress >= 30 && progress < 40) {
            mTvTime.setText("15s");
        } else if (progress >= 40 && progress < 50) {
            mTvTime.setText("20s");
        } else if (progress >= 50 && progress < 60) {
            mTvTime.setText("25s");
        } else if (progress >= 60 && progress < 70) {
            mTvTime.setText("30s");
        } else if (progress >= 70 && progress < 80) {
            mTvTime.setText("1min");
        } else if (progress >= 80 && progress < 90) {
            mTvTime.setText("2min");
        } else if (progress >= 90 && progress < 100) {
            mTvTime.setText("5min");
        } else if (progress >= 100 && progress < 110) {
            mTvTime.setText("10min");
        } else if (progress >= 110 && progress < 120) {
            mTvTime.setText("15min");
        } else if (progress >= 120 && progress < 130) {
            mTvTime.setText("20min");
        } else if (progress >= 130 && progress < 140) {
            mTvTime.setText("30min");
        }
    }

    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processAutoLock(bean);
        } else if (bean.getCMD() == CMD_SET_AUTO_LOCK_TIME) {
            processAutoLockTime(bean);
        }
    }

    private void processAutoLock(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if (state == 0x00) {
            updateLockInfoToService(false);
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveAutoLockStateToLocal() {
        mBleDeviceLocal.setAutoLock(!mBleDeviceLocal.isAutoLock());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void processAutoLockTime(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if (state == 0x00) {
            updateLockInfoToService(true);
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveAutoLockTimeToLocal() {
        mBleDeviceLocal.setSetAutoLockTime(mTime);
        mBeforeTime = mTime;
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private String getTimeString(int time) {
        String timeStr;
        if (time < 60) {
            timeStr = time + "s";
        } else {
            timeStr = (time / 60) + "min";
        }
        return timeStr;
    }

    /**
     * 更新锁服务器存储的数据
     *
     * @param isUpdateTime 更新的是否是自动上锁时间
     */
    private void updateLockInfoToService(boolean isUpdateTime) {
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
        if (isUpdateTime) {
            saveAutoLockTimeToLocal();
        } else {
            saveAutoLockStateToLocal();
        }
        UpdateLockInfoReq req = new UpdateLockInfoReq();
        req.setSn(mBleDeviceLocal.getEsn());
        req.setWifiName(mBleDeviceLocal.getConnectedWifiName());
        req.setSafeMode(0);   // 没有使用这个
        req.setLanguage("en"); // 暂时也没使用这个
        req.setVolume(mBleDeviceLocal.isMute() ? 1 : 0);
        req.setAmMode(mBleDeviceLocal.isAutoLock() ? 0 : 1);
        req.setDuress(mBleDeviceLocal.isDuress() ? 0 : 1);
        req.setMagneticStatus(mBleDeviceLocal.getDoorSensor());
        req.setDoorSensor(mBleDeviceLocal.isOpenDoorSensor()?1:0);
        req.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 0 : 1);
        req.setAutoLockTime(mBleDeviceLocal.getSetAutoLockTime());
        req.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
        req.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
        Timber.e("std46787884445:%s", req.toString());
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
                    if (!TextUtils.isEmpty(msg)) ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    return;
                }
                initUI();
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
