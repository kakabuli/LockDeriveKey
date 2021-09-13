package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * 添加绑定初始化密码成功或是失败界面
 */
public class AddInitPwdNextActivity extends BaseActivity {
    private ImageView mIcon;
    private TextView mTextState;
    private TextView btnCancel;
    private Button btnTryAgain;
    private boolean isGoToAddWifi = false;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.IS_GO_TO_ADD_WIFI)) {
            isGoToAddWifi = intent.getBooleanExtra(Constant.IS_GO_TO_ADD_WIFI, false);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_init_pwd_next_view;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mIcon = findViewById(R.id.activity_add_init_pwd_next_icon);//状态icon
        mTextState = findViewById(R.id.tvTip);//状态提示
        btnCancel = findViewById(R.id.btnCancel);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        if (isGoToAddWifi) {
            //显示成功界面
            mIcon.setBackgroundResource(R.drawable.add_init_pwd_ok_icon);
            mTextState.setText(getText(R.string.add_init_pwd_next_ok));
            btnCancel.setVisibility(View.INVISIBLE);
            btnTryAgain.setText(getString(R.string.complete));
        } else {
            //显示失败界面
            mIcon.setBackgroundResource(R.drawable.add_init_pwd_fail_icon);
            mTextState.setText(getText(R.string.add_init_pwd_next_fail));
            btnCancel.setVisibility(View.VISIBLE);
            btnTryAgain.setText(getString(R.string.try_again));
        }
        applyDebouncingClickListener( btnCancel,btnTryAgain);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnCancel) {
            finish();
            return;
        }
        if (view.getId() == R.id.btnTryAgain) {
            if (!isGoToAddWifi) {
                Intent intent = new Intent(this, AddInitPwdActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
