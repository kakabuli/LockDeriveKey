package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 扫描二维码
 */
public class AddDeviceQRCodeStep2Activity extends BaseActivity {

    private ZBarView mZBarView;
    private LinearLayout mLlLight;
    private boolean isOpenFlashLight = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_qr_code_step2;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device)).getTvTitle().setTextColor(Color.WHITE);
        mLlLight = findViewById(R.id.llLight);
        applyDebouncingClickListener(findViewById(R.id.tvManualInput), mLlLight);
        mZBarView = findViewById(R.id.zBarView);
        mZBarView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String result) {
                Timber.d("onScanQRCodeSuccess 扫描结果：%1s", result);
                if (null != result && !"".equals(result)) {
                    // ESN=S420210110001&MAC=10:98:C3:72:C6:23
                    if (result.indexOf("ESN=") > -1 && result.indexOf("MAC") > -1 && result.indexOf("&") > -1) {
                        Intent intent = new Intent(AddDeviceQRCodeStep2Activity.this, AddDeviceStep2BleConnectActivity.class);
                        intent.putExtra(Constant.PRE_A, Constant.QR_CODE_A);
                        intent.putExtra(Constant.QR_RESULT, result);
                        startActivity(intent);
                        finish();
                    }else{
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Abnormal QR code, please scan again");
                    }
                } else {
                    return;
                }
            }

            @Override
            public void onCameraAmbientBrightnessChanged(boolean isDark) {
                if (isOpenFlashLight) {
                    mLlLight.setVisibility(View.VISIBLE);
                } else {
                    mLlLight.setVisibility(isDark ? View.VISIBLE : View.INVISIBLE);
                }
            }

            @Override
            public void onScanQRCodeOpenCameraError() {
                Timber.e("onScanQRCodeOpenCameraError 打开相机出错了");
            }
        });

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

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.tvManualInput) {
            startActivity(new Intent(this, InputESNActivity.class));
            return;
        }
        if (view.getId() == R.id.llLight) {
            if (isOpenFlashLight) {
                mZBarView.openFlashlight();
            } else {
                mZBarView.closeFlashlight();
            }
            isOpenFlashLight = !isOpenFlashLight;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mZBarView != null) {
            // 打开后置摄像头开始预览，但是并未开始识别
            mZBarView.startCamera();
            // 显示扫描框，并开始识别
            mZBarView.startSpotAndShowRect();
        }
    }

    @Override
    protected void onStop() {
        if (mZBarView != null) {
            mZBarView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
            mZBarView.closeFlashlight();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mZBarView != null) {
            mZBarView.onDestroy(); // 销毁二维码扫描控件
            mZBarView.closeFlashlight();
        }
        super.onDestroy();
    }
}
