package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : login
 */
public class LoginActivity extends BaseActivity {

    private EditText mEtEmail, mEtPwd;
    private boolean isShowPwd = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.sign_in));
        mEtEmail = findViewById(R.id.etEmail);
        mEtPwd = findViewById(R.id.etPwd);
        applyDebouncingClickListener(findViewById(R.id.tvForgotPwd),
                findViewById(R.id.ivEye), findViewById(R.id.btnSignIn));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvForgotPwd) {
            startActivity(new Intent(this, ForgetThePwdActivity.class));
            return;
        }
        if(view.getId() == R.id.ivEye) {
            ImageView ivEye = findViewById(R.id.ivEye);
            ivEye.setImageResource(isShowPwd?R.drawable.ic_login_icon_display:R.drawable.ic_login_icon_hide);
            EditText etPwd = findViewById(R.id.etPwd);
            etPwd.setInputType(isShowPwd?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowPwd = !isShowPwd;
            return;
        }
        if(view.getId() == R.id.btnSignIn) {
            login();
        }
    }

    private void login() {
        String mail = mEtEmail.getText().toString().trim();
        String pwd = mEtPwd.getText().toString();
        // TODO: 2021/1/26 提示语抽离同时修正
        if(TextUtils.isEmpty(mail)) {
            ToastUtils.showShort("Please input your account!");
            return;
        }
        if(!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort("Please input right account!");
            return;
        }
        if(TextUtils.isEmpty(pwd)) {
            ToastUtils.showShort("Please input your password!");
            return;
        }
        MailLoginBeanReq req = new MailLoginBeanReq();
        req.setMail(mail);
        req.setPassword(pwd);
        Observable<MailLoginBeanRsp> observable = HttpRequest
                .getInstance().login(req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<MailLoginBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull MailLoginBeanRsp mailLoginBeanRsp) {
                if(!mailLoginBeanRsp.getCode().equals("200")) {
                    // TODO: 2021/1/26 获取弹出错误的信息
                    Timber.e("登录请求错误了！ code : %1s, msg: %2s",
                            mailLoginBeanRsp.getCode(), mailLoginBeanRsp.getMsg());
                    return;
                }
                if(mailLoginBeanRsp.getData() == null) {
                    return;
                }
                Timber.d("登录成功，token: %1s\n userId: %2s",
                        mailLoginBeanRsp.getData().getToken(), mailLoginBeanRsp.getData().getUid());
                App.getInstance().setUserBean(mailLoginBeanRsp.getData());
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

}
