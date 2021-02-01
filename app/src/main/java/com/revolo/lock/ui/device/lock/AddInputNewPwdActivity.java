package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加新的密码
 */
public class AddInputNewPwdActivity extends BaseActivity {

    private EditText mEtPwd;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_input_new_pwd;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mEtPwd = findViewById(R.id.etPwd);
        applyDebouncingClickListener(findViewById(R.id.btnNext));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            String pwd = mEtPwd.getText().toString().trim();
            if(pwd.length() >= 4 && pwd.length() <= 12) {
                App.getInstance().addWillFinishAct(this);
                Intent intent = new Intent(this, AddNewPwdSelectActivity.class);
                intent.putExtra(Constant.USER_PWD, pwd);
                startActivity(intent);
            } else {
                // TODO: 2021/1/25 抽离文字
                ToastUtils.showShort("Please input right password!");
            }

        }
    }
}
