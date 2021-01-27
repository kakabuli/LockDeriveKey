package com.revolo.lock.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/11
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试选择页面
 */
public class TestSelectActivity extends BaseActivity {

    private String mQRResult;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.QR_RESULT)) {
            mQRResult = intent.getStringExtra(Constant.QR_RESULT);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_test_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        applyDebouncingClickListener(findViewById(R.id.btnTestWifi), findViewById(R.id.btnCommand));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
//        if(TextUtils.isEmpty(mQRResult)) {
//            return;
//        }
        if(view.getId() == R.id.btnTestWifi) {
            Intent intent = new Intent(this, TestWifiActivity.class);
            intent.putExtra(Constant.PRE_A, Constant.QR_CODE_A);
            intent.putExtra(Constant.QR_RESULT, mQRResult);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.btnCommand) {
            Intent intent = new Intent(this, TestCommandActivity.class);
//            intent.putExtra(Constant.PRE_A, Constant.QR_CODE_A);
//            intent.putExtra(Constant.QR_RESULT, mQRResult);
            startActivity(intent);
        }
    }
}
