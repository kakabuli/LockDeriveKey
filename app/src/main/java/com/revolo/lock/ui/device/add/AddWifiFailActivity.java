package com.revolo.lock.ui.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author :
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AddWifiFailActivity extends BaseActivity {

    private String mWifiName;
    private String mWifiPwd;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.WIFI_NAME)) {
            // TODO: 2021/1/22 没有输入wifi name
            finish();
            return;
        }
        if(!intent.hasExtra(Constant.WIFI_PWD)) {
            // TODO: 2021/1/22 没有输入wifi pwd
            finish();
            return;
        }
        mWifiName = intent.getStringExtra(Constant.WIFI_NAME);
        mWifiPwd = intent.getStringExtra(Constant.WIFI_PWD);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi_fail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
        applyDebouncingClickListener(findViewById(R.id.btnReDistribution), findViewById(R.id.btnCancel));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnReDistribution) {
            Intent intent = new Intent(this, WifiConnectActivity.class);
            intent.putExtra(Constant.WIFI_NAME, mWifiName);
            intent.putExtra(Constant.WIFI_PWD, mWifiPwd);
            startActivity(intent);
            finish();
            return;
        }
        if(view.getId() == R.id.btnCancel) {
            finish();
        }
    }
}
