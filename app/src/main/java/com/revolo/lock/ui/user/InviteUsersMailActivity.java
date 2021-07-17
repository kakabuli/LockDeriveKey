package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UserByMailExistsBeanReq;
import com.revolo.lock.bean.respone.UserByMailExistsBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * author : zhougm
 * time   : 2021/7/16
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class InviteUsersMailActivity extends BaseActivity {

    private EditText mEtEmail, mEtFirstName, mEtLastName;
    private boolean isNext = false;
    private String mail;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_invite_users_mail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_invite_users));

        mEtEmail = findViewById(R.id.etEmail);
        mEtFirstName = findViewById(R.id.etFirstName);
        mEtLastName = findViewById(R.id.etLastName);
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            if (isNext) {
                String firstName = mEtFirstName.getText().toString().trim();
                String lastName = mEtLastName.getText().toString().trim();
                if (TextUtils.isEmpty(firstName)) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_first_name);
                    return;
                }
                if (TextUtils.isEmpty(lastName)) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_last_name);
                    return;
                }
                if ((firstName.length() < 2 || firstName.length() > 30) || (lastName.length() < 2 || lastName.length() > 30)) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_modify_user_name_length);
                    return;
                }

                Intent intent = new Intent(InviteUsersMailActivity.this, AddDeviceForSharedUserActivity.class);
                intent.putExtra(Constant.SHARE_USER_MAIL, mail);
                intent.putExtra(Constant.SHARE_USER_FIRST_NAME, firstName);
                intent.putExtra(Constant.SHARE_USER_LAST_NAME, lastName);
                startActivity(intent);
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.t_please_input_right_mail_address));
            }
        });

        mEtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // 失去焦点
                userByMailExists();
            }
        });
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void userByMailExists() {
        if (!checkNetConnectFail()) {
            return;
        }
        mail = mEtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.err_tip_please_input_email);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_mail_address);
            return;
        }
        if (mail.equals(App.getInstance().getUser().getMail())) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_you_not_share_it_with_yourself);
            return;
        }
        UserByMailExistsBeanReq req = new UserByMailExistsBeanReq();
        req.setMail(mail);
        Observable<UserByMailExistsBeanRsp> observable = HttpRequest.getInstance().getUserByMailExists(req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UserByMailExistsBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull UserByMailExistsBeanRsp userByMailExistsBeanRsp) {
                if (userByMailExistsBeanRsp.getData() != null) {
                    if (!userByMailExistsBeanRsp.getData().isExsist()) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.tip_content_mail_not_find));
                        isNext = false;
                    } else {
                        isNext = true;
                    }
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
