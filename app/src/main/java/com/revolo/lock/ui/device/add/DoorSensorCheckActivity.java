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

    private ImageView mIvDoorState;
    private TextView mTvTip, mTvSkip;
    private Button mBtnNext;

    @IntDef(value = {DOOR_CLOSE, DOOR_HALF, DOOR_SUC, DOOR_FAIL})
    private @interface DoorState{}
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
        mBtnNext = findViewById(R.id.btnNext);
        mIvDoorState = findViewById(R.id.ivDoorState);
        mTvTip = findViewById(R.id.tvTip);
        mTvSkip = findViewById(R.id.tvSkip);
        applyDebouncingClickListener(mBtnNext, mTvSkip);
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
                refreshCloseTheDoor();
            }
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            switch (mDoorState) {
                case DOOR_CLOSE:
                    refreshHalfTheDoor();
                    break;
                case DOOR_HALF:
                    isDoorSuc = false;
                    checkDoorSuc();
                    break;
                case DOOR_SUC:
                    gotoAddWifi();
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

    private void gotoAddWifi() {
        startActivity(new Intent(this, AddWifiActivity.class));
        finish();
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

    private boolean isDoorSuc = true;
    private void checkDoorSuc() {
        if(isDoorSuc) {
            mIvDoorState.setImageResource(R.drawable.ic_equipment_img_magnetic_door_success);
            mTvTip.setText(getString(R.string.door_check_suc_tip));
            mBtnNext.setText(getString(R.string.connect_wifi));
            mDoorState = DOOR_SUC;
        } else {
            mDoorState = DOOR_FAIL;
            startActivity(new Intent(this, DoorCheckFailActivity.class));
            finish();
        }

    }

}
