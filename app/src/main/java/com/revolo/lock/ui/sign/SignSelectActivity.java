package com.revolo.lock.ui.sign;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;

import java.util.concurrent.Executor;

import static com.revolo.lock.Constant.REVOLO_SP;

public class SignSelectActivity extends BaseActivity {
    private final Executor executor = mHandler::post;

    private final Handler delayHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String signSelectMode = getIntent().getStringExtra(Constant.SIGN_SELECT_MODE);
            int activitySize = LockAppManager.getAppManager().getActivitySize();
            if (activitySize > 1) {
                finish();
            }
            if (TextUtils.isEmpty(signSelectMode)) {
                verification();
            } else {
                startActivity(new Intent(SignSelectActivity.this, LoginActivity.class));
                finish();
            }
        }
    };

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_sign_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        setStatusBarColor(R.color.white);
        delayHandler.sendEmptyMessageDelayed(0, 2000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void verification() {

        String loginJson = SPUtils.getInstance(REVOLO_SP).getString(Constant.USER_LOGIN_INFO);

        User user = App.getInstance().getUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        boolean isUseTouchId = user.isUseTouchId();
        boolean isUseGestureCode = user.isUseGesturePassword();
        boolean isFaceId = user.isUseFaceId();
        if ((!TextUtils.isEmpty(loginJson) && isFaceId)) {
            startActivity(new Intent(this, FaceAutoLoginActivity.class));
            finish();
        } else if ((!TextUtils.isEmpty(loginJson)) && isUseTouchId) {
            startActivity(new Intent(this, FingerprintAutoLoginActivity.class));
            finish();
        } else if ((!TextUtils.isEmpty(loginJson)) && isUseGestureCode) {
            startActivity(new Intent(this, DrawHandPwdAutoLoginActivity.class));
            finish();
        } else {
            autoLogin();
        }

    }

    private void autoLogin() {
        String loginJson = SPUtils.getInstance(REVOLO_SP).getString(Constant.USER_LOGIN_INFO);
        if (TextUtils.isEmpty(loginJson)) {
            return;
        }
        MailLoginBeanRsp.DataBean dataBean = GsonUtils.fromJson(loginJson, MailLoginBeanRsp.DataBean.class);
        if (dataBean == null) {
            return;
        }
        App.getInstance().setUserBean(dataBean);
        User user = App.getInstance().getUser();
        if (user == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SignSelectActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle extras = getIntent().getExtras();
            if (extras != null) intent.putExtras(extras);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        }, 50);
    }
}
