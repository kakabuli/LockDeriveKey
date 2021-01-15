package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户权限选择
 */
public class AuthorizationManagementActivity extends BaseActivity {

    private String mUserName;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.USER_NAME)) {
            mUserName = intent.getStringExtra(Constant.USER_NAME);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_authorization_management;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_management));
        TextView tvUserTip = findViewById(R.id.tvUserTip);
        if(!TextUtils.isEmpty(mUserName)) {
            tvUserTip.setText(getString(R.string.tip_invite_user, mUserName));
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }
}
