package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.DevicePwd;


/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加新密码名称
 */
public class AddNewPwdNameActivity extends BaseActivity {

    private long mPwdId;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_ID)) {
            mPwdId = intent.getLongExtra(Constant.PWD_ID, -1);
        }
        if(mPwdId == -1) {
            // TODO: 2021/2/21 或者有其他更好的处理方式
            finish();
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
        // TODO: 2021/1/30 后面需要修改，同步存在服务器或者本地数据库
        DevicePwd devicePwd = AppDatabase.getInstance(this).devicePwdDao().findDevicePwdFromId(mPwdId);
        devicePwd.setPwdName(pwdName);
        AppDatabase.getInstance(this).devicePwdDao().update(devicePwd);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 50);
    }

}
