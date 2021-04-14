package com.revolo.lock.ui.device.add;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.util.AppManager;


/**
 * author :
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AddWifiSucActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi_suc;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_wifi));
    }

    @Override
    public void doBusiness() {
        finishPreAct();
        threeSecFinish();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
    }

    private void finishPreAct() {
        AppManager.getInstance().finishActivity(WifiConnectActivity.class);
        AppManager.getInstance().finishActivity(AddWifiActivity.class);
    }

    private void threeSecFinish() {
        // 3秒后销毁
        new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(this::finish), 3000);
    }

}
