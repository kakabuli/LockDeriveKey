package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

public class SignSelectActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_sign_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        applyDebouncingClickListener(findViewById(R.id.btnRegister), findViewById(R.id.btnSignIn));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
            return;
        }
        if(view.getId() == R.id.btnSignIn) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
