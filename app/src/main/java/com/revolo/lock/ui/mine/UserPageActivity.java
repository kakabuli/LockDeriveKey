package com.revolo.lock.ui.mine;

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
import com.revolo.lock.bean.TestUserBean;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户页面
 */
public class UserPageActivity extends BaseActivity {

    private TestUserBean mTestUserBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.USER_INFO)) {
            mTestUserBean = intent.getParcelableExtra(Constant.USER_INFO);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_user_page;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_user_page));
        applyDebouncingClickListener(findViewById(R.id.clUserName),
                findViewById(R.id.clEmail), findViewById(R.id.clChangePwd), findViewById(R.id.btnLogout));
        if(mTestUserBean != null) {
            TextView tvUserName = findViewById(R.id.tvUserName);
            TextView tvEmailAddress = findViewById(R.id.tvEmailAddress);
            tvUserName.setText(TextUtils.isEmpty(mTestUserBean.getUserName())?"":mTestUserBean.getUserName());
            tvEmailAddress.setText(TextUtils.isEmpty(mTestUserBean.getEmail())?"":mTestUserBean.getEmail());
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clUserName) {
            startActivity(new Intent(this, ModifyUserNameActivity.class));
            return;
        }
        if(view.getId() == R.id.clEmail) {
            startActivity(new Intent(this, ModifyEmailActivity.class));
            return;
        }
        if(view.getId() == R.id.clChangePwd) {
            startActivity(new Intent(this, ModifyPasswordActivity.class));
            return;
        }
        if(view.getId() == R.id.btnLogout) {
            // TODO: 2021/1/19 退出登录
        }
    }
}
