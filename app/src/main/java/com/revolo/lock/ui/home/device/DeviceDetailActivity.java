package com.revolo.lock.ui.home.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.TestLockBean;

/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备详情页面
 */
public class DeviceDetailActivity extends BaseActivity {

    private TestLockBean mTestLockBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.LOCK_DETAIL)) {
            mTestLockBean = intent.getParcelableExtra(Constant.LOCK_DETAIL);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar("Homepage");
        setStatusBarColor(R.color.white);
        initDevice();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.llNotification) {
            startActivity(new Intent(this, OperationRecordsActivity.class));
            return;
        }
        if(view.getId() == R.id.llPwd) {
            startActivity(new Intent(this, PasswordListActivity.class));
            return;
        }
        if(view.getId() == R.id.llUser) {
            startActivity(new Intent(this, UserManagementActivity.class));
            return;
        }
        if(view.getId() == R.id.llSetting) {
            // TODO: 2021/1/13
        }
    }

    private void initDevice() {
        if(mTestLockBean == null) {
            return;
        }
        ImageView ivLockState = findViewById(R.id.ivLockState);
        ImageView ivNetState = findViewById(R.id.ivNetState);
        ImageView ivDoorState = findViewById(R.id.ivDoorState);
        TextView tvNetState = findViewById(R.id.tvNetState);
        TextView tvDoorState = findViewById(R.id.tvDoorState);
        LinearLayout llLowBattery = findViewById(R.id.llLowBattery);
        LinearLayout llNotification = findViewById(R.id.llNotification);
        LinearLayout llPwd = findViewById(R.id.llPwd);
        LinearLayout llUser = findViewById(R.id.llUser);
        LinearLayout llSetting = findViewById(R.id.llSetting);
        LinearLayout llDoorState = findViewById(R.id.llDoorState);
        TextView tvPrivateMode = findViewById(R.id.tvPrivateMode);

        applyDebouncingClickListener(llNotification, llPwd, llUser, llSetting);

        if(mTestLockBean.getModeState() == 2) {
            ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_privacymodel));
            tvPrivateMode.setVisibility(View.VISIBLE);
            llDoorState.setVisibility(View.GONE);
        } else {
            tvPrivateMode.setVisibility(View.GONE);
            llDoorState.setVisibility(View.VISIBLE);
            if(mTestLockBean.getDoorState() == 1) {
                ivLockState.setImageResource(R.drawable.ic_home_img_lock_open);
                ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
                tvDoorState.setText(R.string.tip_opened);
            } else {
                ivLockState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img_lock_close));
                ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
                tvDoorState.setText(R.string.tip_closed);
            }
        }
        if(mTestLockBean.getInternetState() == 1) {
            ivNetState.setImageResource(R.drawable.ic_home_icon_wifi);
        } else {
            ivNetState.setImageResource(R.drawable.ic_home_icon_bluetooth);
        }
        tvNetState.setText(getString(R.string.tip_online));

    }

}
