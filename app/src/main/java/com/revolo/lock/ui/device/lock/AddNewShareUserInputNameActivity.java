package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加新分享用户输入名字
 */
public class AddNewShareUserInputNameActivity extends BaseActivity {

    private EditText mEtUserName;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_new_share_user_input_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_user));
        applyDebouncingClickListener(findViewById(R.id.btnAddUser));
        mEtUserName = findViewById(R.id.etUserName);

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnAddUser) {
            String userName = mEtUserName.getText().toString().trim();
            if(TextUtils.isEmpty(userName)) {
                return;
            }
            Intent intent = new Intent(this, AuthorizationManagementActivity.class);
            intent.putExtra(Constant.USER_NAME, userName);
            startActivity(intent);
        }
    }
}
