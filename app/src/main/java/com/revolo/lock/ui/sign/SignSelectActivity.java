package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;

import static com.revolo.lock.Constant.REVOLO_SP;

public class SignSelectActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_sign_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        applyDebouncingClickListener(findViewById(R.id.btnRegister), findViewById(R.id.btnSignIn));
        setStatusBarColor(R.color.white);
    }

    @Override
    public void doBusiness() {
        autoLogin();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
            return;
        }
        if(view.getId() == R.id.btnSignIn) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void autoLogin() {
        String loginJson = SPUtils.getInstance(REVOLO_SP).getString(Constant.USER_LOGIN_INFO);
        if(TextUtils.isEmpty(loginJson)) {
            return;
        }
        MailLoginBeanRsp.DataBean dataBean = GsonUtils.fromJson(loginJson, MailLoginBeanRsp.DataBean.class);
        if(dataBean == null) {
            return;
        }
        App.getInstance().setUserBean(dataBean);
        User user = App.getInstance().getUser();
        if(user == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SignSelectActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 500);
    }
}
