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
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_DOOR_SENSOR_CALIBRATION;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 *          步骤：关闭门磁->开门->关门->开门->虚掩->开启门磁
 */
public class DoorSensorCheckActivity extends BaseActivity {
    // TODO: 2021/2/22 需要存储到本地数据库
    private ImageView mIvDoorState;
    private TextView mTvTip, mTvSkip, mTvStep;
    private Button mBtnNext;

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
    }

    @Override
    public void doBusiness() {
        initBleListener();
        sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE);
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
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_OPEN);
                    break;
                case DOOR_CLOSE:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_CLOSE);
                    break;
                case DOOR_HALF:
                    sendCommand(BleCommandState.DOOR_CALIBRATION_STATE_HALF);
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

            @Override
            public void onAuthSuc() {

            }
        });
    }
    
    private void changedDoor(BleResultBean bleResultBean) {
        if(bleResultBean.getCMD() == CMD_DOOR_SENSOR_CALIBRATION) {
            if(bleResultBean.getPayload()[0] == 0x00) {
                // 排除掉第一次发送禁用门磁指令的状态反馈
                if(mCalibrationState == BleCommandState.DOOR_CALIBRATION_STATE_CLOSE_SE) {
                    return;
                }
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

            } else {
                mDoorState = DOOR_FAIL;
                Intent intent = new Intent(this, DoorCheckFailActivity.class);
                intent.putExtra(Constant.DEVICE_ID, mDeviceId);
                startActivity(intent);
                finish();
            }
        }
    }

}
