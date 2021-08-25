package com.revolo.lock.ui.sign;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

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
import com.revolo.lock.util.FingerprintUtils;

import java.util.concurrent.Executor;

import timber.log.Timber;

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
                return;
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
            showBiometricPrompt(loginJson, isUseGestureCode);
        } else if ((!TextUtils.isEmpty(loginJson)) && isUseTouchId) {
            if (android.os.Build.VERSION.SDK_INT > 27) {
                showBiometricPrompt(loginJson, isUseGestureCode);
            } else {
                FingerprintUtils fingerprintUtils = new FingerprintUtils(new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        //多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间）
                        //errorCode是失败的次数
                        if (errorCode == 3) {
                            if ((!TextUtils.isEmpty(loginJson)) && isUseGestureCode) {
                                gestureCode();
                            } else {
                                startActivity(new Intent(SignSelectActivity.this, LoginActivity.class));
                                finish();
                            }
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
            }
        } else if ((!TextUtils.isEmpty(loginJson)) && isUseGestureCode) {
            gestureCode();
        } else {
            autoLogin();
        }

    }

    private void gestureCode() {
        Intent intent = new Intent(this, DrawHandPwdAutoLoginActivity.class);
        Bundle extras = getIntent().getExtras();
        if (extras != null) intent.putExtras(extras);
        startActivity(intent);
        finish();
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

    //生物认证的setting
    private void showBiometricPrompt(String loginJson, boolean isUseGestureCode) {
        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(" ") //设置大标题
                        .setSubtitle("") // 设置标题下的提示
                        .setNegativeButtonText("More Login") //设置取消按钮
                        .build();

        //需要提供的参数callback
        BiometricPrompt biometricPrompt = new BiometricPrompt(SignSelectActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            //各种异常的回调
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Timber.e("Authentication error: %s", errString);
                if (errString.equals("More Login")) { // 取消
                    if ((!TextUtils.isEmpty(loginJson)) && isUseGestureCode) {
                        gestureCode();
                    } else {
                        startActivity(new Intent(SignSelectActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }

            //认证成功的回调
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                autoLogin();
            }

            //认证失败的回调
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Timber.e("Authentication failed");
            }
        });

        // 显示认证对话框
        biometricPrompt.authenticate(promptInfo);
    }
}
