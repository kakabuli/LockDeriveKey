package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.dialog.iosloading.MoreLoginPopup;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.util.FingerprintUtils;

import static com.revolo.lock.Constant.REVOLO_SP;

/**
 * author : zhougm
 * time   : 2021/9/6
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class FingerprintAutoLoginActivity extends BaseActivity {

    private MoreLoginPopup moreLoginPopup;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_finger_print_auto_login;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_finger_print_Login));
        FingerprintUtils fingerprintUtils = new FingerprintUtils(new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间）
                //errorCode是失败的次数
                if (errorCode == 3) {
                    startActivity(new Intent(FingerprintAutoLoginActivity.this, LoginActivity.class));
                    finish();
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

        ImageView imageView = findViewById(R.id.ivAvatar);
        String avatarUrl = App.getInstance().getUser().getAvatarUrl();
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        //缓存
                .skipMemoryCache(false)
                .error(R.drawable.mine_personal_img_headportrait_default);           //错误图片
        Glide.with(this)
                .load(avatarUrl)
                .apply(requestOptions)
                .into(imageView);

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
