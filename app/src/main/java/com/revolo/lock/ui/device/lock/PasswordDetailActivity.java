package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestPwdBean;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码详情页面
 */
public class PasswordDetailActivity extends BaseActivity {

    private TestPwdBean mTestPwdBean;
    private TextView mTvPwdName, mTvPwd, mTvPwdCharacteristic, mTvCreationDate;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_DETAIL)) {
            mTestPwdBean = intent.getParcelableExtra(Constant.PWD_DETAIL);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_pwd_details));
        applyDebouncingClickListener(findViewById(R.id.ivEditPwdName));
        mTvPwdName = findViewById(R.id.tvPwdName);
        mTvPwd = findViewById(R.id.tvPwd);
        mTvCreationDate = findViewById(R.id.tvCreationDate);
        mTvPwdCharacteristic  = findViewById(R.id.tvPwdCharacteristic);
    }

    @Override
    public void doBusiness() {
        initDetail();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivEditPwdName) {
            startActivity(new Intent(this, ChangePwdNameActivity.class));
        }
    }

    private void initDetail() {
        if(mTestPwdBean != null) {
            mTvPwdName.setText(mTestPwdBean.getPwdName());
            mTvPwd.setText(mTestPwdBean.getPwd());
            mTvPwdCharacteristic.setText(mTestPwdBean.getPwdCharacteristic());
            mTvCreationDate.setText(mTestPwdBean.getCreateDate());
        }
    }
}
