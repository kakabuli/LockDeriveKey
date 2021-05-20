package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.util.FingerprintUtils;

import static com.revolo.lock.Constant.REVOLO_SP;

public class SignSelectActivity extends BaseActivity {

    private static final int REQUEST_CODE_DRAW_GESTURE_CODE = 1999;
    private ConstraintLayout constraintLayout;

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
        constraintLayout = findViewById(R.id.activity_sign_select_view);
        constraintLayout.setVisibility(View.GONE);
        verification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        constraintLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
            return;
        }
        if (view.getId() == R.id.btnSignIn) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /* if(requestCode == REQUEST_CODE_DRAW_GESTURE_CODE) {
            if(resultCode == RESULT_OK) {
                autoLogin();
            }
        }*/
    }

    private void verification() {
        User user = App.getInstance().getUser();
        if (user == null) {
            constraintLayout.setVisibility(View.VISIBLE);
            return;
        }
        boolean isUseTouchId = user.isUseTouchId();
        boolean isUseGestureCode = user.isUseGesturePassword();
        if (isUseTouchId) {
            constraintLayout.setVisibility(View.VISIBLE);
            FingerprintUtils fingerprintUtils = new FingerprintUtils(new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    //多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间）
                    //errorCode是失败的次数
                    if (errorCode == 3) {
                        gestureCode(isUseGestureCode);
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    //指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    //指纹密码验证成功
                    autoLogin();
                }

                @Override
                public void onAuthenticationFailed() {
                    //指纹验证失败，指纹识别失败，可再验，错误原因为：该指纹不是系统录入的指纹。
                }
            });
            fingerprintUtils.openFingerprintAuth();
        } else {
            constraintLayout.setVisibility(View.GONE);
            gestureCode(isUseGestureCode);
        }

    }

    private void gestureCode(boolean isUseGestureCode) {
        if (isUseGestureCode) {
            Intent intent = new Intent(this, DrawHandPwdAutoLoginActivity.class);
            startActivity(intent);
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
            startActivity(intent);
            finish();
        }, 50);
    }
}
