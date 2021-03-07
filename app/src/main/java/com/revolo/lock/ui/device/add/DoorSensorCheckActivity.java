package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockSetMagneticResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 *          步骤：关闭门磁->开门->关门->开门->虚掩->开启门磁
 */
public class DoorSensorCheckActivity extends BaseActivity {

    private ImageView mIvDoorState;
    private TextView mTvTip, mTvSkip, mTvStep;
    private Button mBtnNext;
    private BleDeviceLocal mBleDeviceLocal;

    @IntDef(value = {DOOR_OPEN, DOOR_CLOSE, DOOR_HALF, DOOR_SUC, DOOR_FAIL, DOOR_OPEN_AGAIN})
    private @interface DoorState{}
    private static final int DOOR_OPEN = 1;
    private static final int DOOR_CLOSE = 2;
    private static final int DOOR_HALF = 3;
    private static final int DOOR_SUC = 4;
    private static final int DOOR_FAIL = 5;
    private static final int DOOR_OPEN_AGAIN = 6;

    @BleCommandState.DoorCalibrationState
    private int mCalibrationState = BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE;

    @DoorState
    private int mDoorState = DOOR_OPEN;

    private long mDeviceId = -1L;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.DEVICE_ID)) {
            mDeviceId = intent.getLongExtra(Constant.DEVICE_ID, -1L);
        }
        if(mDeviceId == -1L) {
            // TODO: 2021/2/22 做处理
            finish();
        }
        mBleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(mDeviceId);
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_door_sensor_check;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_magnet_alignment));
        mBtnNext = findViewById(R.id.btnNext);
        mIvDoorState = findViewById(R.id.ivDoorState);
        mTvTip = findViewById(R.id.tvTip);
        mTvSkip = findViewById(R.id.tvSkip);
        mTvStep = findViewById(R.id.tvStep);
        applyDebouncingClickListener(mBtnNext, mTvSkip);
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
        } else {
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
            if(bleBean != null) {
                bleBean.setOnBleDeviceListener(mOnBleDeviceListener);
            }
            sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
        }
        // 初始化默认第一步执行开门
        isOpenAgain = false;
        refreshOpenTheDoor();
    }

    @Override
    public void onBackPressed() {
        // 步骤：关闭门磁->开门->关门->开门->虚掩->开启门磁
        if(mDoorState == DOOR_OPEN) {
            super.onBackPressed();
        } else {
            if(mDoorState == DOOR_CLOSE) {
                isOpenAgain = false;
                refreshOpenTheDoor();
            } else if(mDoorState == DOOR_OPEN_AGAIN) {
                refreshCloseTheDoor();
            } else if(mDoorState == DOOR_HALF) {
                isOpenAgain = true;
                refreshOpenTheDoor();
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            switch (mDoorState) {
                case DOOR_OPEN:
                case DOOR_OPEN_AGAIN:
                    if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    }
                    break;
                case DOOR_CLOSE:
                    if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    }
                    break;
                case DOOR_HALF:
                    if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_HALF);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_HALF);
                    }
                    break;
                case DOOR_SUC:
                    if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                        publishSetMagnetic(mBleDeviceLocal.getEsn(), BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
                    } else {
                        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
                    }
                    break;
                case DOOR_FAIL:
                    break;

            }
            return;
        }
        if(view.getId() == R.id.tvSkip) {
            gotoAddWifi();
        }
    }

    /*------------------------------------ UI -----------------------------------*/

    private void gotoAddWifi() {
        mBleDeviceLocal.setOpenDoorSensor(true);
        Intent intent = new Intent(this, AddWifiActivity.class);
        intent.putExtra(Constant.DEVICE_ID, mDeviceId);
        startActivity(intent);
        finish();
    }

    private boolean isOpenAgain = false;

    private void refreshOpenTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_open);
        mTvTip.setText(getString(R.string.open_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.open_door_step_1_3_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mDoorState = isOpenAgain?DOOR_OPEN_AGAIN:DOOR_OPEN;
        mTvSkip.setVisibility(isOpenAgain?View.GONE:View.VISIBLE);
    }

    private void refreshCloseTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_close);
        mTvTip.setText(getString(R.string.close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.close_door_step_2_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_CLOSE;
    }

    private void refreshHalfTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_cover_up);
        mTvTip.setText(getString(R.string.half_close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvStep.setText(getString(R.string.half_door_step_4_tip));
        mTvStep.setVisibility(View.VISIBLE);
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_HALF;
    }

    private void refreshDoorSuc() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
        mTvTip.setText(getString(R.string.door_check_suc_tip));
        mBtnNext.setText(getString(R.string.connect_wifi));
        mTvSkip.setVisibility(View.GONE);
        mTvStep.setText("");
        mTvStep.setVisibility(View.INVISIBLE);
        mDoorState = DOOR_SUC;
    }

    private void refreshCurrentUI() {
        runOnUiThread(() -> {
            switch (mDoorState) {
                case DOOR_OPEN:
                    refreshCloseTheDoor();
                    break;
                case DOOR_CLOSE:
                    isOpenAgain = true;
                    refreshOpenTheDoor();
                    break;
                case DOOR_OPEN_AGAIN:
                    refreshHalfTheDoor();
                    break;
                case DOOR_HALF:
                    refreshDoorSuc();
                    break;
                case DOOR_SUC:
                    gotoAddWifi();
                    break;
                case DOOR_FAIL:
                    break;
            }
        });
    }

    /*---------------------------------- MQTT ----------------------------------*/

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
                    saveDoorSensorStateToLocal(mode);
                    refreshCurrentUI();
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

    /*--------------------------------- 蓝牙 -----------------------------------*/

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("sendCommand bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("sendCommand bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if(bleBean.getPwd1() == null) {
            Timber.e("sendCommand bleBean.getPwd1() == null");
            return;
        }
        if(bleBean.getPwd3() == null) {
            Timber.e("sendCommand bleBean.getPwd3() == null");
            return;
        }
        mCalibrationState = doorState;
        App.getInstance()
                .writeControlMsg(BleCommandFactory
                        .doorCalibration(doorState, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        changedDoor(bleResultBean);
    };

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
                Timber.e("initBleListener 传输过来的蓝牙mac地址与当前的不匹配，mac: %1s, local mac: %2s",
                        mac, mBleDeviceLocal.getMac());
                return;
            }
            BleBean bleBean = App.getInstance().getBleBeanFromMac(mac);
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
            BleResultProcess.processReceivedData(value,
                    bleBean.getPwd1(),
                    bleBean.getPwd3(),
                    bleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

        }

        @Override
        public void onAuthSuc(@NotNull String mac) {

        }

    };
    
    private void changedDoor(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if(bleResultBean.getPayload()[0] == 0x00) {
                // 排除掉第一次发送禁用门磁指令的状态反馈
                if(mCalibrationState == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
                    return;
                }
                saveDoorSensorStateToLocal(mCalibrationState);
                refreshCurrentUI();
            } else {
                gotoFailAct();
            }
        }
    }

    private void saveDoorSensorStateToLocal(@BleCommandState.DoorCalibrationState int state) {
        if(state == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
            mBleDeviceLocal.setOpenDoorSensor(false);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        } else if(state == BleCommandState.DOOR_CALIBRATION_STATE_START_SE) {
            mBleDeviceLocal.setOpenDoorSensor(true);
            AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
        }
    }

    private void gotoFailAct() {
        mDoorState = DOOR_FAIL;
        Intent intent = new Intent(this, DoorCheckFailActivity.class);
        intent.putExtra(Constant.DEVICE_ID, mDeviceId);
        startActivity(intent);
        finish();
    }

}
