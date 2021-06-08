package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.BuildConfig;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.MainActivity;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.Constant.USER_MAIL;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : login
 */
public class LoginActivity extends BaseActivity {

    private EditText mEtEmail, mEtPwd;
    private boolean isShowPwd = true;
    private String emailName = "";

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
        mEtEmail.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    mEtEmail.setText(emailName);
                } else {
                    // 此处为失去焦点时的处理内容
                    emailName = mEtEmail.getText().toString();
                    if (null != emailName && !"".equals(emailName) && emailName.length() > 15) {
                        String hintText = emailName.substring(0, 5) + "..." + emailName.substring(emailName.length() - 7, emailName.length());
                        mEtEmail.setText(hintText);
                    } else {
                        mEtEmail.setText(emailName);
                    }
                }
            }
        });
        mEtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    emailName = s.toString();
                } else {
                    emailName = "";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEtPwd = findViewById(R.id.etPwd);
        applyDebouncingClickListener(findViewById(R.id.tvForgotPwd),
                findViewById(R.id.ivEye), findViewById(R.id.btnSignIn));

        if (getIntent().getBooleanExtra(Constant.IS_SHOW_DIALOG, false)) {
            //TODO:是否弹出token失效弹窗
            tokenDialog();
        }
        String mail = SPUtils.getInstance(REVOLO_SP).getString(USER_MAIL);
        if (!TextUtils.isEmpty(mail)) {
            emailName = mail;
            mEtEmail.setText(mail);
        } else {
            if (BuildConfig.DEBUG) {
                emailName = "zhouguimin@kaadas.com";
                mEtEmail.setText("zhouguimin@kaadas.com");
            }
            if (BuildConfig.DEBUG) {
                mEtPwd.setText("zgm123456");
            }
        }
        initLoading("Loading...");
    }

    /**
     * 弹出token失效弹窗
     */
    private void tokenDialog() {

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.tvForgotPwd) {
            startActivity(new Intent(this, ForgetThePwdActivity.class));
            return;
        }
        if (view.getId() == R.id.ivEye) {
            openOrClosePwdEye();
            return;
        }
        if (view.getId() == R.id.btnSignIn) {
            login();
        }
    }

    private void openOrClosePwdEye() {
        ImageView ivEye = findViewById(R.id.ivEye);
        ivEye.setImageResource(isShowPwd ? R.drawable.ic_login_icon_display : R.drawable.ic_login_icon_hide);
        EditText etPwd = findViewById(R.id.etPwd);
        etPwd.setInputType(isShowPwd ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        isShowPwd = !isShowPwd;
    }

    private void login() {
        if (!checkNetConnectFail()) {
            return;
        }
        String mail = emailName.trim();
        String pwd = mEtPwd.getText().toString();
        // TODO: 2021/1/26 提示语抽离同时修正
        if (TextUtils.isEmpty(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_account);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_account);
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_pwd);
            return;
        }
        if (!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_pwd);
            return;
        }
        showLoading();
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
                dismissLoading();
                processLoginRsp(mailLoginBeanRsp, mail);
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

    private void processLoginRsp(@NonNull MailLoginBeanRsp mailLoginBeanRsp, String mail) {
        if (!mailLoginBeanRsp.getCode().equals("200")) {
            // TODO: 2021/1/26 获取弹出错误的信息
            Timber.e("processLoginRsp 登录请求错误了！ code : %1s, msg: %2s",
                    mailLoginBeanRsp.getCode(), mailLoginBeanRsp.getMsg());
            ToastUtils.showShort(mailLoginBeanRsp.getMsg());
            return;
        }
        if (mailLoginBeanRsp.getData() == null) {
            Timber.e("processLoginRsp mailLoginBeanRsp.getData() == null");
            return;
        }
        ThreadUtils.getSinglePool().execute(() -> {
            if (mailLoginBeanRsp.getData() == null) {
                Timber.e("processLoginRsp mailLoginBeanRsp.getData() == null");
                return;
            }
            updateUser(mail, mailLoginBeanRsp.getData());
            Timber.d("processLoginRsp 登录成功，token: %1s\n userId: %2s",
                    mailLoginBeanRsp.getData().getToken(), mailLoginBeanRsp.getData().getUid());
            App.getInstance().setUserBean(mailLoginBeanRsp.getData());
            saveLoginBeanToLocal(mailLoginBeanRsp);
            runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ActivityUtils.finishActivity(SignSelectActivity.class);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                finish();
            }, 50));
        });
    }

    private void saveLoginBeanToLocal(@NonNull MailLoginBeanRsp mailLoginBeanRsp) {
        String loginJson = GsonUtils.toJson(mailLoginBeanRsp.getData());
        SPUtils.getInstance(REVOLO_SP).put(Constant.USER_LOGIN_INFO, loginJson);
    }

    private void updateUser(String mail, @NotNull MailLoginBeanRsp.DataBean rsp) {
        User user = App.getInstance().getUserFromLocal(mail);
        if (user == null) {
            user = new User();
            user.setMail(mail);
            user.setFirstName(rsp.getFirstName());
            user.setLastName(rsp.getLastName());
            user.setRegisterTime(TimeUtils.string2Millis(rsp.getInsertTime()) / 1000);
            user.setAvatarUrl(rsp.getAvatarPath());
            AppDatabase.getInstance(this).userDao().insert(user);
        } else {
            user.setFirstName(rsp.getFirstName());
            user.setLastName(rsp.getLastName());
            user.setRegisterTime(TimeUtils.string2Millis(rsp.getInsertTime()) / 1000);
            user.setAvatarUrl(rsp.getAvatarPath());
            AppDatabase.getInstance(this).userDao().update(user);
        }
    }
}
