package com.revolo.lock.util;

import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import androidx.annotation.RequiresPermission;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * author :
 * time   : 2021/3/19
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class FingerprintUtils {

    private FingerprintManager mFingerprintManager;
    private final CancellationSignal mCancellationSignal;
    private final FingerprintManager.AuthenticationCallback mSelfCancelled;

    public FingerprintUtils(FingerprintManager.AuthenticationCallback selfCancelled) {
        mSelfCancelled = selfCancelled;
        mCancellationSignal = new CancellationSignal();
    }

    public void openFingerprintAuth() {
        // 开启指纹识别功能
        if(judgeFingerprintIsCorrect()) {
            if(mFingerprintManager != null) {
                mFingerprintManager.authenticate(null, mCancellationSignal, 0, mSelfCancelled, null);
            }
        }
    }

    public FingerprintManager getFingerprintManagerOrNull() {
        if (App.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            mFingerprintManager = App.getInstance().getSystemService(FingerprintManager.class);
            return mFingerprintManager;
        } else {
            return null;
        }
    }

    // TODO: 2021/3/19 有部分机型可能存在需要申请该权限才可以使用
    @RequiresPermission(USE_FINGERPRINT)
    public boolean judgeFingerprintIsCorrect() {
        mFingerprintManager = getFingerprintManagerOrNull();
        if (mFingerprintManager != null) {
            // 判断硬件是否支持指纹识别
            if(!mFingerprintManager.isHardwareDetected()) {
                ToastUtils.showShort(R.string.t_the_device_does_not_support_fingerprint_recognition);
                return false;
            }
            // 判断是否有指纹录入
            if(!mFingerprintManager.hasEnrolledFingerprints()) {
                ToastUtils.showShort(R.string.t_no_fingerprints);
                return false;
            }
            return true;
        }
        return false;
    }

}
