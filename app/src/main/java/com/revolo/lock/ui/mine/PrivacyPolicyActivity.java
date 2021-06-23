package com.revolo.lock.ui.mine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ui.sign.TermActivity;

public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_privacy_policy;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.privacy_policy));
        applyDebouncingClickListener(findViewById(R.id.clPrivacyAgreement), findViewById(R.id.clUserAgreement));
    }

    @Override
    public void doBusiness() {

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onDebouncingClick(@NonNull View view) {
        Intent intent = new Intent(this, TermActivity.class);
        switch (view.getId()) {
            case R.id.clPrivacyAgreement:
                intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_PRIVACY);
                break;
            case R.id.clUserAgreement:
                intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_USER);
                break;
        }
        startActivity(intent);
    }
}
