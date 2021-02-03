package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
 * desc   : 添加新密码名称
 */
public class AddNewPwdNameActivity extends BaseActivity {

    private byte mNum;
    private final byte mMaxValue = (byte) 0xff;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.KEY_PWD_NUM)) {
            // TODO: 2021/1/30 要跟软件确认num的最大值是多少
            mNum = intent.getByteExtra(Constant.KEY_PWD_NUM, mMaxValue);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_new_pwd_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        applyDebouncingClickListener(findViewById(R.id.btnComplete), findViewById(R.id.tvAddNextTime));
    }

    @Override
    public void doBusiness() {
        App.getInstance().finishPreActivities();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnComplete) {
            sendPwdDataToService();
            return;
        }
        if(view.getId() == R.id.tvAddNextTime) {
            finish();
        }
    }

    private void sendPwdDataToService() {
        // TODO: 2021/1/30 把密码等信息发送到服务器
        savePwdName();
    }

    private void savePwdName() {
        EditText etPwdName = findViewById(R.id.etPwdName);
        String pwdName = etPwdName.getText().toString().trim();
        if(TextUtils.isEmpty(pwdName)) {
            // TODO: 2021/1/30 修改提示语
            ToastUtils.showShort("Please Input Password Name!");
            return;
        }
        // TODO: 2021/1/30 暂时存储在这里，后面需要修改，存在服务器或者本地数据库
        App.getInstance().getCacheDiskUtils().put("pwdName"+mNum, pwdName);
        finish();
    }

}
