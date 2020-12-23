package com.revolo.lock.ui.sign;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class RegisterAddAvatar extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register_add_avatar;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }
}
