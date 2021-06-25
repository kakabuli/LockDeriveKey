package com.revolo.lock.ui.device.add;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;


/**
 * author :
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class AddWifiSucActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_wifi_suc;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_connect_wifi));
    }

    @Override
    public void doBusiness() {
        finishPreAct();
        threeSecFinish();
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
    public void onDebouncingClick(@NonNull View view) {
    }

    private void finishPreAct() {
        ActivityUtils.finishActivity(WifiConnectActivity.class);
        ActivityUtils.finishActivity(AddWifiActivity.class);
        ActivityUtils.finishActivity(InputESNActivity.class);
    }

    private void threeSecFinish() {
        // 3秒后销毁
        new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(this::finish), 3000);
    }

}
