package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 */
public class DoorSensorCheckActivity extends BaseActivity {

    private ImageView mIvDoorState;
    private TextView mTvTip, mTvSkip;
    private Button mBtnNext;

    @IntDef(value = {DOOR_OPEN, DOOR_CLOSE, DOOR_HALF, DOOR_SUC, DOOR_FAIL})
    private @interface DoorState{}
    private static final int DOOR_OPEN = 1;
    private static final int DOOR_CLOSE = 2;
    private static final int DOOR_HALF = 3;
    private static final int DOOR_SUC = 4;
    private static final int DOOR_FAIL = 5;

    @BleCommandState.DoorCalibrationState
    private int mCalibrationState = BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE;

    @DoorState
    private int mDoorState = DOOR_CLOSE;

    @Override
    public void initData(@Nullable Bundle bundle) {

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
        applyDebouncingClickListener(mBtnNext, mTvSkip);
    }

    @Override
    public void doBusiness() {
        initBleListener();
        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
    }

    @Override
    public void onBackPressed() {
        if(mDoorState == DOOR_CLOSE) {
            super.onBackPressed();
        } else {
            if(mDoorState == DOOR_HALF) {
                refreshOpenTheDoor();
            } else if(mDoorState == DOOR_OPEN) {
                refreshCloseTheDoor();
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            switch (mDoorState) {
                case DOOR_CLOSE:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    break;
                case DOOR_HALF:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_HALF);
                    break;
                case DOOR_OPEN:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    break;
                case DOOR_SUC:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_START_SE);
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

    private void sendCommand(@BleCommandState.DoorCalibrationState int doorState) {
        mCalibrationState = doorState;
        App.getInstance()
                .writeControlMsg(BleCommandFactory
                        .doorCalibration(doorState,
                                App.getInstance().getBleBean().getPwd1(),
                                App.getInstance().getBleBean().getPwd3()));
    }

    private void gotoAddWifi() {
        startActivity(new Intent(this, AddWifiActivity.class));
        finish();
    }

    private void refreshOpenTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_open);
        mTvTip.setText(getString(R.string.open_the_door));
        mBtnNext.setText(getString(R.string.next));
        mDoorState = DOOR_OPEN;
    }

    private void refreshCloseTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_close);
        mTvTip.setText(getString(R.string.close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvSkip.setVisibility(View.VISIBLE);
        mDoorState = DOOR_CLOSE;
    }

    private void refreshHalfTheDoor() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_cover_up);
        mTvTip.setText(getString(R.string.half_close_the_door));
        mBtnNext.setText(getString(R.string.next));
        mTvSkip.setVisibility(View.GONE);
        mDoorState = DOOR_HALF;
    }

    private void refreshDoorSuc() {
        mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
        mTvTip.setText(getString(R.string.door_check_suc_tip));
        mBtnNext.setText(getString(R.string.connect_wifi));
        mDoorState = DOOR_SUC;
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        changedDoor(bleResultBean);
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
        });
    }
    
    private void changedDoor(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == 0x1F) {
            if(bleResultBean.getPayload()[0] == 0x00) {
                // TODO: 2021/2/5 发送禁用也会返回状态
                runOnUiThread(() -> {
                    switch (mDoorState) {
                        case DOOR_CLOSE:
                            refreshOpenTheDoor();
                            break;
                        case DOOR_HALF:
                            refreshDoorSuc();
                            break;
                        case DOOR_OPEN:
                            refreshHalfTheDoor();
                            break;
                        case DOOR_SUC:
                            gotoAddWifi();
                            break;
                        case DOOR_FAIL:
                            break;
                    }
                });

            } else {
                mDoorState = DOOR_FAIL;
                startActivity(new Intent(this, DoorCheckFailActivity.class));
                finish();
            }
        }
    }

}
