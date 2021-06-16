package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.sign.TermActivity;

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
        tvVersion.setText(getString(R.string.about_ver, AppUtils.getAppVersionName()));
        TextView tvContact = findViewById(R.id.tvContact);
        // TODO: 2021/3/8 后期从服务器获取
        tvContact.setText("service@irevolo.com");
        applyDebouncingClickListener(findViewById(R.id.clPrivacyAgreement), findViewById(R.id.clUserAgreement), findViewById(R.id.ivLogo));
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
        if (view.getId() == R.id.clUserAgreement) {
            Intent intent = new Intent(this, TermActivity.class);
            intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_USER);
            startActivity(intent);
        } else if (view.getId() == R.id.clPrivacyAgreement) {
            Intent intent = new Intent(this, TermActivity.class);
            intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_PRIVACY);
            startActivity(intent);
        } else if (view.getId() == R.id.ivLogo) {
            launchAppDetail("com.revolo.lock","com.android.vending");
        }
    }

    //参数名：app包名以及google play包名。

    public void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
