package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

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
        initLoading("Logging out...");
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
            showLogoutDialog();
        }
    }

    private void showLogoutDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_log_out));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            logout();
        });
        dialog.show();
    }

    private void logout() {
        if(App.getInstance().getUserBean() == null) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            return;
        }
        showLoading();
        Observable<LogoutBeanRsp> observable = HttpRequest.getInstance().logout(token);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LogoutBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LogoutBeanRsp logoutBeanRsp) {
                dismissLoading();
                String code = logoutBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    String msg = logoutBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                        return;
                    }
                }
                if(App.getInstance().getMainActivity() != null) {
                    App.getInstance().getMainActivity().finish();
                }
                finish();
                startActivity(new Intent(UserPageActivity.this, LoginActivity.class));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });

    }
}
