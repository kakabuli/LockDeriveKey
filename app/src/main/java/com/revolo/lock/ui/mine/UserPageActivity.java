package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestUserBean;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户页面
 */
public class UserPageActivity extends BaseActivity {

    private User mUser;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_user_page;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_user_page));
        applyDebouncingClickListener(findViewById(R.id.clUserName), findViewById(R.id.clChangePwd), findViewById(R.id.btnLogout));
        mUser = App.getInstance().getUser();
    }

    private void refreshUserUI() {
        runOnUiThread(() -> {
            if(mUser != null) {
                TextView tvUserName = findViewById(R.id.tvUserName);
                TextView tvEmailAddress = findViewById(R.id.tvEmailAddress);
                String userName = mUser.getUserName();
                // TODO: 2021/3/7 名字后面需要更改其他显示
                tvUserName.setText(TextUtils.isEmpty(userName)?"John":userName);
                String email = mUser.getMail();
                tvEmailAddress.setText(TextUtils.isEmpty(email)?"":email);
            }
        });
    }

    @Override
    public void doBusiness() {
        refreshUserUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clUserName) {
            startActivity(new Intent(this, ModifyUserNameActivity.class));
            return;
        }
        if(view.getId() == R.id.clChangePwd) {
            startActivity(new Intent(this, ModifyPasswordActivity.class));
            return;
        }
        if(view.getId() == R.id.btnLogout) {
            SelectDialog dialog = new SelectDialog(this);
            dialog.setMessage(getString(R.string.dialog_tip_log_out));
            dialog.setOnCancelClickListener(v -> dialog.dismiss());
            dialog.setOnConfirmListener(v -> {
                dialog.dismiss();
                if(App.getInstance().getMainActivity() != null) {
                    App.getInstance().getMainActivity().finish();
                }
                finish();
                startActivity(new Intent(this, LoginActivity.class));
            });
            dialog.show();
        }
    }
}
