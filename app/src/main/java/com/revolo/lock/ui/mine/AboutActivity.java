package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 关于页面
 */
public class AboutActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_about;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_about));
    }

    @Override
    public void doBusiness() {
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(AppUtils.getAppVersionName());
        TextView tvContact = findViewById(R.id.tvContact);
        // TODO: 2021/3/8 后期从服务器获取
        tvContact.setText("revolodeveloper@gmail.com");
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }
}
