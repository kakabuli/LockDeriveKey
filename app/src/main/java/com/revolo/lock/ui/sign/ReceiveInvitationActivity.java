package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

public class ReceiveInvitationActivity extends BaseActivity {
    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_receive_invitation;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        setStatusBarColor(R.color.white);
        useCommonTitleBar(getString(R.string.title_receive_invitation));
        applyDebouncingClickListener(findViewById(R.id.btnInvitation));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnInvitation) {
            startActivity(new Intent(this, ReceiveInvitationCreateActivity.class));
        }
    }
}
