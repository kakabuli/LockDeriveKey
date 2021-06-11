package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.util.FingerprintUtils;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设置页面
 */
public class SettingActivity extends BaseActivity {

    private ImageView ivGestureCodeEnable, ivEnableTouchIDEnable, ivEnableFaceIDEnable;
    private User mUser;
    private static final int REQUEST_CODE_OPEN_GESTURE_CODE = 1999;
    private static final int REQUEST_CODE_CLOSE_GESTURE_CODE = 1888;
    private int mFaceIDCode = -1;

    private ConstraintLayout mClEnableTouchID, mClEnableFaceID, clChangeGesturePassword;
    private FingerprintUtils mFingerprintUtils;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mUser = App.getInstance().getUser();
        if (mUser == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_setting));
        ivGestureCodeEnable = findViewById(R.id.ivGestureCodeEnable);
        ivEnableTouchIDEnable = findViewById(R.id.ivEnableTouchIDEnable);
        ivEnableFaceIDEnable = findViewById(R.id.ivEnableFaceIDEnable);
        mClEnableFaceID = findViewById(R.id.clEnableFaceID);
        mClEnableTouchID = findViewById(R.id.clEnableTouchID);
        clChangeGesturePassword = findViewById(R.id.clChangeGesturePassword);
        applyDebouncingClickListener(ivGestureCodeEnable, ivEnableTouchIDEnable, ivEnableFaceIDEnable, clChangeGesturePassword);
        mFingerprintUtils = new FingerprintUtils(new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间）
                //errorCode是失败的次数

            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                //指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                //指纹密码验证成功
                boolean isUseFingerprint = mUser.isUseTouchId();
                mUser.setUseTouchId(!isUseFingerprint);
                AppDatabase.getInstance(SettingActivity.this).userDao().update(mUser);
                refreshUI();
            }

            @Override
            public void onAuthenticationFailed() {
                //指纹验证失败，指纹识别失败，可再验，错误原因为：该指纹不是系统录入的指纹。
            }
        });

        initFaceID();
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
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivGestureCodeEnable) {
            if (mUser.isUseGesturePassword()) {
                Intent intent = new Intent(this, CloseDrawHandPwdActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CLOSE_GESTURE_CODE);
            } else {
                Intent intent = new Intent(this, OpenDrawHandPwdActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OPEN_GESTURE_CODE);
            }
            return;
        } else if (view.getId() == R.id.ivEnableTouchIDEnable) {
            if (android.os.Build.VERSION.SDK_INT > 27) {
                biometricSet(false);
            } else {
                mFingerprintUtils.openFingerprintAuth();
            }
            return;
        } else if (view.getId() == R.id.ivEnableFaceIDEnable) {
            // TODO: 2021/3/19 faceId
            biometricSet(true);
        } else if (view.getId() == R.id.clChangeGesturePassword) {
            if (mUser.isUseGesturePassword()) {
                Intent intent = new Intent(this, OpenDrawHandPwdActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OPEN_GESTURE_CODE);
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Please open Gesture password!");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GESTURE_CODE) {
            if (resultCode == RESULT_OK) {
                mUser.setUseGesturePassword(true);
                AppDatabase.getInstance(this).userDao().update(mUser);
                refreshUI();
            }
        } else if (requestCode == REQUEST_CODE_CLOSE_GESTURE_CODE) {
            if (resultCode == RESULT_OK) {
                mUser.setUseGesturePassword(false);
                AppDatabase.getInstance(this).userDao().update(mUser);
                refreshUI();
            }
        }
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            ivGestureCodeEnable.setImageResource(mUser.isUseGesturePassword() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            ivEnableTouchIDEnable.setImageResource(mUser.isUseTouchId() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
            ivEnableFaceIDEnable.setImageResource(mUser.isUseFaceId() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
        });
    }

    /**
     * 生物识别设置
     */
    private void biometricSet(boolean isFace) {

        switch (mFaceIDCode) {
            case 0:
                if (isFace) {
                    mUser.setUseFaceId(!mUser.isUseFaceId());
                } else {
                    mUser.setUseTouchId(!mUser.isUseTouchId());
                }
                AppDatabase.getInstance(this).userDao().update(mUser);
                refreshUI();
                break;
            case 1:
            case -1:
            case 2:
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Device not Support!");
                break;
            case 3:
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Device not find FaceID!");
                break;
        }
    }

    private void initFaceID() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Timber.d("允许生物识别");
                mFaceIDCode = 0;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Timber.d("不允许生物识别");
                mFaceIDCode = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Timber.d("没有可用的生物特征功能");
                mFaceIDCode = 2;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Timber.d("没有录入生物识别数据");
                mFaceIDCode = 3;
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Timber.d("BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED");
                mFaceIDCode = 4;
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Timber.d("BIOMETRIC_ERROR_UNSUPPORTED");
                mFaceIDCode = 5;
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Timber.d("BIOMETRIC_STATUS_UNKNOWN");
                mFaceIDCode = 6;
                break;
        }
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }
}
