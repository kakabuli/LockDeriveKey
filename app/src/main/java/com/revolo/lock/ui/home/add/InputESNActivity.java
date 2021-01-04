package com.revolo.lock.ui.home.add;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/3
 * E-mail : wengmaowei@kaadas.com
 * desc   : 输入ESN码
 */
public class InputESNActivity extends BaseActivity {

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_input_esn;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_manual_input));
        setStatusBarColor(R.color.white);
        applyDebouncingClickListener(findViewById(R.id.btnNext));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            String esn = ((EditText) findViewById(R.id.etESN)).getText().toString().trim();
            if(TextUtils.isEmpty(esn)) {
                ToastUtils.showShort(R.string.t_please_input_product_esn);
                return;
            }
            Intent intent = new Intent(this, AddDeviceStep2BleConnectActivity.class);
            intent.putExtra(Constant.PRE_A, Constant.INPUT_ESN_A);
            intent.putExtra(Constant.ESN, esn);
            startActivity(intent);
        }
    }

}
