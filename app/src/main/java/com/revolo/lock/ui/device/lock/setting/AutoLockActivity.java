package com.revolo.lock.ui.device.lock.setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AmModeParams;
import com.revolo.lock.mqtt.bean.publishbean.attrparams.AutoLockTimeParams;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoRspBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetLockAttrAutoTimeRspBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
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
    private TextView mTvTime, mTvDetectionLock, mTvTip;
    private int mTime = 0;
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
        useCommonTitleBar(getString(R.string.title_auto_lock));
        mSeekBar = findViewById(R.id.seekBar);
        mTvTime = findViewById(R.id.tvTime);
        mTvTip = findViewById(R.id.tvTip);
        mTvDetectionLock = findViewById(R.id.tvDetectionLock);
        mClSetLockTime = findViewById(R.id.clSetLockTime);
        mIvAutoLockEnable = findViewById(R.id.ivAutoLockEnable);
        mIvDetectionLockEnable = findViewById(R.id.ivDetectionLockEnable);
        applyDebouncingClickListener(mIvAutoLockEnable, mIvDetectionLockEnable);
        initLoading("Setting...");
        mSeekBar.setMax(140);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChange(progress);
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
        initUI();
    }

    @Override
    public void doBusiness() {
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
                // TODO: 2021/2/8 查询一下当前设置
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivAutoLockEnable) {
            if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                publishOpenOrCloseAutoLock(mBleDeviceLocal.getEsn());
            } else {
                openOrCloseAutoLock();
            }
            return;
        }
        if(view.getId() == R.id.ivDetectionLockEnable) {
            openOrCloseDetectionLock();
        }
    }

    private void initUI() {
        runOnUiThread(() -> {
            mIvAutoLockEnable.setImageResource(mBleDeviceLocal.isAutoLock()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            mTvDetectionLock.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mIvDetectionLockEnable.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mClSetLockTime.setVisibility(mBleDeviceLocal.isAutoLock()?View.VISIBLE:View.GONE);
            mTvTime.setText(getTimeString(mTime));
            mIvDetectionLockEnable
                    .setImageResource(mBleDeviceLocal.isDetectionLock()?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            mSeekBar.setProgress(getProgressFromTime(mBleDeviceLocal.getSetAutoLockTime()));
            mTvTip.setText(getString(mTime==0?R.string.tip_the_timer_will_start_when_your_door_is_closed:R.string.tip_door_will_be_locked_when_time_is_up));
        });

    }

    private Disposable mOpenOrCloseAutoLockDisposable;

    private void publishOpenOrCloseAutoLock(String wifiID) {
        if(mMQttService == null) {
            Timber.e("publishOpenOrCloseAutoLock mMQttService == null");
            return;
        }
        toDisposable(mOpenOrCloseAutoLockDisposable);
        @LocalState.AutoState int auto = mBleDeviceLocal.isAutoLock()?LocalState.AUTO_STATE_CLOSE:LocalState.AUTO_STATE_OPEN;
        showLoading();
        AmModeParams amModeParams = new AmModeParams();
        amModeParams.setAmMode(auto);
        mOpenOrCloseAutoLockDisposable = mMQttService.mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, amModeParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mOpenOrCloseAutoLockDisposable);
                    processOpenOrCloseAutoLock(mqttData);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mOpenOrCloseAutoLockDisposable);
    }

    private void processOpenOrCloseAutoLock(MqttData mqttData) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishOpenOrCloseAutoLock mqttData.getFunc() is empty");
            return;
        }
        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR)) {
            if(!mqttData.getPayload().contains("amMode")) {
                // 不是该MQTT的数据 不处理
                return;
            }
            dismissLoading();
            Timber.d("publishOpenOrCloseAutoLock 设置属性: %1s", mqttData);
            WifiLockSetLockAttrAutoRspBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrAutoRspBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }
            if(bean == null) {
                Timber.e("publishOpenOrCloseAutoLock bean == null");
                return;
            }
            if(bean.getParams() == null) {
                Timber.e("publishOpenOrCloseAutoLock bean.getParams() == null");
                return;
            }
            if(bean.getCode() != 200) {
                Timber.e("publishOpenOrCloseAutoLock code : %1d", bean.getCode());
                return;
            }
            saveAutoLockStateToLocal();
            initUI();
        }
        Timber.d("publishOpenOrCloseAutoLock %1s", mqttData.toString());
    }

    private Disposable mAutoLockTimeDisposable;

    private void publishAutoLockTime(String wifiID, int time) {
        if(mMQttService == null) {
            Timber.e("publishAutoLockTime mMQttService == null");
            return;
        }
        showLoading();
        AutoLockTimeParams autoLockTimeParams = new AutoLockTimeParams();
        autoLockTimeParams.setAutoLockTime(time);
        toDisposable(mAutoLockTimeDisposable);
        mAutoLockTimeDisposable = mMQttService.mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setLockAttr(wifiID, autoLockTimeParams,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .filter(mqttData -> mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .subscribe(mqttData -> {
                    toDisposable(mAutoLockTimeDisposable);
                    processAutoLockTime(mqttData);
                }, e -> {
                    // TODO: 2021/3/3 错误处理
                    // 超时或者其他错误
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mAutoLockTimeDisposable);
    }

    private void processAutoLockTime(MqttData mqttData) {
        if(TextUtils.isEmpty(mqttData.getFunc())) {
            Timber.e("publishAutoLockTime mqttData.getFunc() is empty");
            return;
        }
        if(mqttData.getFunc().equals(MqttConstant.SET_LOCK_ATTR)) {
            dismissLoading();
            Timber.d("publishAutoLockTime 设置属性: %1s", mqttData);
            WifiLockSetLockAttrAutoTimeRspBean bean;
            try {
                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetLockAttrAutoTimeRspBean.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                return;
            }
            if(bean == null) {
                Timber.e("publishAutoLockTime bean == null");
                return;
            }
            if(bean.getParams() == null) {
                Timber.e("publishAutoLockTime bean.getParams() == null");
                return;
            }
            if(bean.getCode() != 200) {
                Timber.e("publishAutoLockTime code : %1d", bean.getCode());
                return;
            }
            saveAutoLockTimeToLocal();
            initUI();
        }
        Timber.d("publishAutoLockTime %1s", mqttData.toString());
    }

    private void openOrCloseDetectionLock() {
        // TODO: 2021/2/22 服务器开启，或者本地开关 2021/2/22 开启服务通知
        mBleDeviceLocal.setDetectionLock(!mBleDeviceLocal.isDetectionLock());
        initUI();
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    // TODO: 2021/2/8 要接收回调处理
    private void openOrCloseAutoLock() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("openOrCloseAutoLock bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("openOrCloseAutoLock bleBean.getPwd3() == null");
            return;
        }
        byte[] value = new byte[1];
        value[0] = (byte) (mBleDeviceLocal.isAutoLock()?0x01:0x00);
        App.getInstance().writeControlMsg(BleCommandFactory
                .lockParameterModificationCommand((byte) 0x04, (byte) 0x01, value, bleBean.getPwd1(),
                        bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
    }

    private void stopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress >= 0 && progress < 10) {
            if (!mBleDeviceLocal.isOpenDoorSensor()) {
                mTvTime.setText("10s");
                mTime = 10;
                seekBar.setProgress(20);
            } else {
                mTvTime.setText("立刻");
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
        } else if(progress >= 30 && progress < 40) {
            mTvTime.setText("15s");
            mTime = 15;
        } else if(progress >= 40 && progress < 50) {
            mTvTime.setText("20s");
            mTime = 20;
        } else if(progress >= 50 && progress < 60) {
            mTvTime.setText("25s");
            mTime = 25;
        } else if(progress >= 60 && progress < 70) {
            mTvTime.setText("30s");
            mTime = 30;
        } else if(progress >= 70 && progress < 80) {
            mTvTime.setText("1min");
            mTime = 60;
        } else if(progress >= 80 && progress < 90) {
            mTvTime.setText("2min");
            mTime = 2*60;
        } else if(progress >= 90 && progress < 100) {
            mTvTime.setText("5min");
            mTime = 5*60;
        } else if(progress >= 100 && progress < 110) {
            mTvTime.setText("10min");
            mTime = 10*60;
        } else if(progress >= 110 && progress < 120) {
            mTvTime.setText("15min");
            mTime = 15*60;
        } else if(progress >= 120 && progress < 130) {
            mTvTime.setText("20min");
            mTime = 20*60;
        } else if(progress >= 130 && progress < 140) {
            mTvTime.setText("30min");
            mTime = 30*60;
        }
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishAutoLockTime(mBleDeviceLocal.getEsn(), mTime);
        } else {
            setAutoLockTimeFromBle();
        }
    }

    private void setAutoLockTimeFromBle() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("setAutoLockTimeFromBle bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("setAutoLockTimeFromBle bleBean.getPwd3() == null");
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory
                .setAutoLockTime(mTime, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
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
            case 2*60:
                return 85;
            case 5*60:
                return 95;
            case 10*60:
                return 105;
            case 15*60:
                return 115;
            case 20*60:
                return 125;
            case 30*60:
                return 140;
            default:
                return 0;
        }
    }

    private void progressChange(int progress) {
        if (progress >= 0 && progress < 10) {
            mTvTime.setText("0s");
        } else if (progress >= 10 && progress < 20) {
            mTvTime.setText("5s");
        } else if(progress >= 20 && progress < 30) {
            mTvTime.setText("10s");
        } else if(progress >= 30 && progress < 40) {
            mTvTime.setText("15s");
        } else if(progress >= 40 && progress < 50) {
            mTvTime.setText("20s");
        } else if(progress >= 50 && progress < 60) {
            mTvTime.setText("25s");
        } else if(progress >= 60 && progress < 70) {
            mTvTime.setText("30s");
        } else if(progress >= 70 && progress < 80) {
            mTvTime.setText("1min");
        } else if(progress >= 80 && progress < 90) {
            mTvTime.setText("2min");
        } else if(progress >= 90 && progress < 100) {
            mTvTime.setText("5min");
        } else if(progress >= 100 && progress < 110) {
            mTvTime.setText("10min");
        } else if(progress >= 110 && progress < 120) {
            mTvTime.setText("15min");
        } else if(progress >= 120 && progress < 130) {
            mTvTime.setText("20min");
        } else if(progress >= 130 && progress < 140) {
            mTvTime.setText("30min");
        }
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
            BleResultProcess.processReceivedData(value, bleBean.getPwd1(),
                    bleBean.getPwd3(), bleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }

    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processAutoLock(bean);
        } else if(bean.getCMD() == CMD_SET_AUTO_LOCK_TIME) {
            processAutoLockTime(bean);
        }
    }

    private void processAutoLock(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            saveAutoLockStateToLocal();
            initUI();
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
        if(state == 0x00) {
            saveAutoLockTimeToLocal();
            initUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void saveAutoLockTimeToLocal() {
        mBleDeviceLocal.setSetAutoLockTime(mTime);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private String getTimeString(int time) {
        String timeStr;
        if(time < 60) {
            timeStr = time+"s";
        } else {
            timeStr = (time/60) + "min";
        }
        return timeStr;
    }

}
