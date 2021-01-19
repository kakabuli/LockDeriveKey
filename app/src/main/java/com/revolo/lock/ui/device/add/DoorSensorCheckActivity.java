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

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门磁校验
 */
public class DoorSensorCheckActivity extends BaseActivity {

    private ImageView ivDoorState;
    private TextView tvTip;
    private Button btnNext;

    @IntDef(value = {DOOR_OPEN, DOOR_CLOSE, DOOR_HALF, DOOR_SUC, DOOR_FAIL})
    private @interface DoorState{}
    private static final int DOOR_OPEN = 1;
    private static final int DOOR_CLOSE = 2;
    private static final int DOOR_HALF = 3;
    private static final int DOOR_SUC = 4;
    private static final int DOOR_FAIL = 5;

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
        btnNext = findViewById(R.id.btnNext);
        ivDoorState = findViewById(R.id.ivDoorState);
        tvTip = findViewById(R.id.tvTip);
        applyDebouncingClickListener(btnNext);
    }

    @Override
    public void doBusiness() {

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
                    refreshOpenTheDoor();
                    break;
                case DOOR_HALF:
                    isDoorSuc = false;
                    checkDoorSuc();
                    break;
                case DOOR_OPEN:
                    refreshHalfTheDoor();
                    break;
                case DOOR_SUC:
                    startActivity(new Intent(this, AddWifiActivity.class));
                    finish();
                    break;
                case DOOR_FAIL:
                    break;
            }
        }
    }

    private void refreshCloseTheDoor() {
        ivDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_close);
        tvTip.setText(getString(R.string.close_the_door));
        btnNext.setText(getString(R.string.next));
        mDoorState = DOOR_CLOSE;
    }

    private void refreshOpenTheDoor() {
        ivDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_open);
        tvTip.setText(getString(R.string.open_the_door));
        btnNext.setText(getString(R.string.next));
        mDoorState = DOOR_OPEN;
    }

    private void refreshHalfTheDoor() {
        ivDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_cover_up);
        tvTip.setText(getString(R.string.half_close_the_door));
        btnNext.setText(getString(R.string.next));
        mDoorState = DOOR_HALF;
    }

    private boolean isDoorSuc = true;
    private void checkDoorSuc() {
        if(isDoorSuc) {
            ivDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
            tvTip.setText(getString(R.string.door_check_suc_tip));
            btnNext.setText(getString(R.string.connect_wifi));
            mDoorState = DOOR_SUC;
        } else {
            mDoorState = DOOR_FAIL;
            startActivity(new Intent(this, DoorCheckFailActivity.class));
            finish();
        }

    }

}
