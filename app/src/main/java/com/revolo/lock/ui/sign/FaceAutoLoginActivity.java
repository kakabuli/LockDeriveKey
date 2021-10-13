package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.dialog.iosloading.MoreLoginPopup;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;

import java.util.concurrent.Executor;

import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;

/**
 * author : zhougm
 * time   : 2021/9/6
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class FaceAutoLoginActivity extends BaseActivity {

    private final Executor executor = mHandler::post;
    private MoreLoginPopup moreLoginPopup;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_face_auto_login;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_face_Login));

        showBiometricPrompt();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    //生物认证的setting
    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(" ") //设置大标题
                        .setSubtitle("") // 设置标题下的提示
                        .setNegativeButtonText("More Login") //设置取消按钮
                        .build();

        //需要提供的参数callback
        BiometricPrompt biometricPrompt = new BiometricPrompt(FaceAutoLoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            //各种异常的回调
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Timber.e("Authentication error: %s", errString);
                if (errString.equals("More Login")) { // 取消
                    startActivity(new Intent(FaceAutoLoginActivity.this, LoginActivity.class));
                    finish();
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

        moreLoginPopup = new MoreLoginPopup(this);
        moreLoginPopup.setEmailLoginListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            if (moreLoginPopup != null) {
                moreLoginPopup.dismiss();
            }
            finish();
        });
        moreLoginPopup.setCancelOnClickListener(v -> {
            if (moreLoginPopup != null) {
                moreLoginPopup.dismiss();
            }
        });

        findViewById(R.id.tv_more_unlock).setOnClickListener(v -> {
            if (moreLoginPopup != null) {
                moreLoginPopup.setPopupGravity(Gravity.BOTTOM);
                moreLoginPopup.showPopupWindow();
            }
        });
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle extras = getIntent().getExtras();
            if (extras != null) intent.putExtras(extras);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        }, 50);
    }
}
