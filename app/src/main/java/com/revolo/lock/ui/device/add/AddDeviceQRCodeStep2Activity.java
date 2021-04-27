package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;

import com.king.zxing.CameraScan;
import com.king.zxing.DefaultCameraScan;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 扫描二维码
 */
public class AddDeviceQRCodeStep2Activity extends BaseActivity {

    private CameraScan mCameraScan;

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
        LinearLayout llLight = findViewById(R.id.llLight);
        applyDebouncingClickListener(findViewById(R.id.tvManualInput), llLight);

        PreviewView previewView = findViewById(R.id.previewView);
        mCameraScan = new DefaultCameraScan(this, previewView);
        mCameraScan.setOnScanResultCallback(result -> {
            gotoBleConnectAct(result.getText());
            return false;
        })
                .bindFlashlightView(llLight)
                .setVibrate(true)
                .startCamera();

    }

    private void gotoBleConnectAct(String result) {
        Timber.d("onScanQRCodeSuccess 扫描结果：%1s", result);
        Intent intent = new Intent(AddDeviceQRCodeStep2Activity.this, AddDeviceStep2BleConnectActivity.class);
        intent.putExtra(Constant.PRE_A, Constant.QR_CODE_A);
        intent.putExtra(Constant.QR_RESULT, result);
        startActivity(intent);
        finish();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvManualInput) {
            startActivity(new Intent(this, InputESNActivity.class));
            return;
        }
        if(view.getId() == R.id.llLight) {
            if(mCameraScan != null) {
                mCameraScan.enableTorch(!mCameraScan.isTorchEnabled());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCameraScan != null) {
            mCameraScan.startCamera();
        }
    }

    @Override
    protected void onStop() {
        if(mCameraScan != null) {
            mCameraScan.stopCamera();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mCameraScan != null) {
            mCameraScan.enableTorch(false);
            mCameraScan.release();
        }
        super.onDestroy();
    }
}
