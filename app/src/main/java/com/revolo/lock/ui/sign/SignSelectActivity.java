package com.revolo.lock.ui.sign;

import android.annotation.SuppressLint;
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
        setStatusBarColor(R.color.white);
        applyDebouncingClickListener(findViewById(R.id.btnInvitation));
        applyDebouncingClickListener(findViewById(R.id.btnRegister));
        applyDebouncingClickListener(findViewById(R.id.btnSignIn));
    }

    @Override
    public void doBusiness() {

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onDebouncingClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.btnInvitation:
                startActivity(new Intent(this, ReceiveInvitationActivity.class));
                break;
            case R.id.btnRegister:
                break;
            case R.id.btnSignIn:
                break;
            default:
                break;
        }
    }
}
