package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.DoorSensorCheckActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 */
public class DoorMagnetAlignmentActivity extends BaseActivity {

    // TODO: 2021/3/6 进页面先MQTT读取门磁状态
    private ConstraintLayout mClTip;
    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvDoorMagneticEnable;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.BLE_DEVICE)) {
            // TODO: 2021/2/22 处理
            finish();
            return;
        }
        mBleDeviceLocal = intent.getParcelableExtra(Constant.BLE_DEVICE);
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_magnet_alignment;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_magnet_alignment));
        mClTip = findViewById(R.id.clTip);
        mIvDoorMagneticEnable = findViewById(R.id.ivDoorMagneticEnable);
        applyDebouncingClickListener(mClTip, mIvDoorMagneticEnable);
        refreshDoorMagneticEnableState();
    }

    private void refreshDoorMagneticEnableState() {
        mIvDoorMagneticEnable
                .setImageResource(mBleDeviceLocal.isOpenDoorSensor()
                        ?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
    }

    @Override
    public void doBusiness() {
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            initBleListener();
            checkDoorSensorState();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clTip) {
            Intent intent = new Intent(this, DoorSensorCheckActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mBleDeviceLocal.getId());
            startActivity(intent);
            finish();
            return;
        }
        if(view.getId() == R.id.ivDoorMagneticEnable) {
            if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                publishSetMagnetic(mBleDeviceLocal.getEsn(), mBleDeviceLocal.isOpenDoorSensor()?
                        BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE:BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
            } else {
                sendCommand(mBleDeviceLocal.isOpenDoorSensor()?
                        BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE:BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
            }
        }
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        changedDoorSensorState(bleResultBean);
    };

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onReceivedValue(String uuid, byte[] value) {
                if(value == null) {
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc() {

            }
        });
    }

    private void changedDoorSensorState(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if(bleResultBean.getPayload()[0] == 0x00) {
                saveDoorSensorStateToLocal();
                refreshDoorMagneticEnableState();
            } else {
                // TODO: 2021/3/6 出错的情况
            }
        } else if(bleResultBean.getCMD() == CMD_LOCK_INFO) {
            // TODO: 2021/3/7 获取门磁状态
            byte[] state = BleByteUtil.byteToBit(bleResultBean.getPayload()[7]);
            byte doorSensorState = state[3];
            // 更新最新的门磁状态
            mBleDeviceLocal.setOpenDoorSensor(doorSensorState == 0x01);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
            refreshDoorMagneticEnableState();
        }
    }

    private void saveDoorSensorStateToLocal() {
        mBleDeviceLocal.setOpenDoorSensor(!mBleDeviceLocal.isOpenDoorSensor());
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        if(App.getInstance().getBleBean() == null) {
            Timber.e("sendCommand App.getInstance().getBleBean() == null");
            return;
        }
        byte[] pwd1 = App.getInstance().getBleBean().getPwd1();
        if(pwd1 == null) {
            Timber.e("sendCommand wd1 == null");
            return;
        }
        byte[] pwd3 = App.getInstance().getBleBean().getPwd3();
        if(pwd3 == null) {
            Timber.e("sendCommand pwd3 == null");
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.doorCalibration(doorState, pwd1, pwd3));
    }

    private void checkDoorSensorState() {
        if(App.getInstance().getBleBean() == null) {
            Timber.e("checkDoorSensorState App.getInstance().getBleBean() == null");
            return;
        }
        byte[] pwd1 = App.getInstance().getBleBean().getPwd1();
        if(pwd1 == null) {
            Timber.e("checkDoorSensorState wd1 == null");
            return;
        }
        byte[] pwd3 = App.getInstance().getBleBean().getPwd3();
        if(pwd3 == null) {
            Timber.e("checkDoorSensorState pwd3 == null");
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.checkLockBaseInfoCommand(pwd1, pwd3));
    }

    public void publishSetMagnetic(String wifiID, @BleCommandState.DoorCalibrationState int mode) {
        showLoading();
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.setMagnetic(wifiID, mode, BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            Timber.e("publishSetMagnetic mqttData.getFunc() is empty");
                            return;
                        }
                        if(mqttData.getFunc().equals(MqttConstant.SET_MAGNETIC)) {
                            dismissLoading();
                            Timber.d("设置门磁: %1s", mqttData);
                            WifiLockSetMagneticResponseBean bean;
                            try {
                                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockSetMagneticResponseBean.class);
                            } catch (JsonSyntaxException e) {
                                Timber.e(e);
                                return;
                            }
                            if(bean == null) {
                                Timber.e("bean == null");
                                return;
                            }
                            if(bean.getParams() == null) {
                                Timber.e("bean.getParams() == null");
                                return;
                            }
                            if(bean.getCode() != 200) {
                                Timber.e("code : %1d", bean.getCode());
                                return;
                            }
                            saveDoorSensorStateToLocal();
                            refreshDoorMagneticEnableState();

                        }
                        Timber.d("%1s", mqttData.toString());
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        // TODO: 2021/3/3 错误处理
                        // 超时或者其他错误
                        dismissLoading();
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
