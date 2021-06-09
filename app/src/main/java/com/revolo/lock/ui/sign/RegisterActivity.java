package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.request.MailRegisterBeanReq;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.util.LinkClickableSpan;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;


/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : 注册页面
 */
public class RegisterActivity extends BaseActivity {

    private boolean isSelected = false;
    private boolean isShowPwd = true;
    private boolean isCountdown = false;
    private EditText mEtEmail;
    private TextView mTvGetCode;
    private String emailName="";

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
        mTvGetCode = findViewById(R.id.tvGetCode);
        applyDebouncingClickListener(findViewById(R.id.btnStartCreating),
                findViewById(R.id.ivEye),
                findViewById(R.id.ivSelect),
                mTvGetCode);

        TextView tvAgreement = findViewById(R.id.tvAgreement);
        String agreementStr = getString(R.string.terms_of_use);
        SpannableString spannableString = new SpannableString(agreementStr);
        LinkClickableSpan span = new LinkClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(RegisterActivity.this, TermActivity.class);
                intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_USER);
                startActivity(intent);
            }
        };
        spannableString.setSpan(span, 0, agreementStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvAgreement.append(getString(R.string.i_agree_to));
        tvAgreement.append(spannableString);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        initLoading("Registering...");

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
                if (s.toString().indexOf("...") > 0) {
                    return;
                }
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


    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnStartCreating) {
            register();
            return;
        }
        if (view.getId() == R.id.ivEye) {
            ImageView ivEye = findViewById(R.id.ivEye);
            ivEye.setImageResource(isShowPwd ? R.drawable.ic_login_icon_display : R.drawable.ic_login_icon_hide);
            EditText etPwd = findViewById(R.id.etPwd);
            etPwd.setInputType(isShowPwd ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowPwd = !isShowPwd;
            return;
        }
        if (view.getId() == R.id.ivSelect) {
            ImageView ivSelect = findViewById(R.id.ivSelect);
            isSelected = !isSelected;
            ivSelect.setImageResource(isSelected ? R.drawable.ic_sign_in_icon_selected : R.drawable.ic_sign_in_icon_default);
            return;
        }
        if (view.getId() == R.id.tvGetCode) {
            if (!isCountdown) {
                getCode();
            }
        }
    }

    private void getCode() {
        if (!checkNetConnectFail()) {
            return;
        }
        String mail = emailName.trim();
        if (TextUtils.isEmpty(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.err_tip_please_input_email);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_mail_address);
            return;
        }
        GetCodeBeanReq req = new GetCodeBeanReq();
        req.setMail(mail);
        req.setWorld(2);
        Observable<GetCodeBeanRsp> observable = HttpRequest.getInstance().getCode(req);
        isCountdown = true;
        mCountDownTimer.start();
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetCodeBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetCodeBeanRsp getCodeBeanRsp) {
                if (TextUtils.isEmpty(getCodeBeanRsp.getCode())) {
                    Timber.e("getCodeBeanRsp.getCode() is null");
                    return;
                }
                if (!getCodeBeanRsp.getCode().equals("200")) {
                    String msg = getCodeBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", getCodeBeanRsp.getCode(), msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void register() {
        if (!checkNetConnectFail()) {
            return;
        }
        String mail = emailName.trim();
        if (TextUtils.isEmpty(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.err_tip_please_input_email);
            return;
        }
        if (!RegexUtils.isEmail(mail)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_mail_address);
            return;
        }
        String tokens = ((EditText) findViewById(R.id.etVerification)).getText().toString().trim();
        if (TextUtils.isEmpty(tokens)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.err_tip_please_input_verification_code);
            return;
        }
        String pwd = ((EditText) findViewById(R.id.etPwd)).getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_pwd);
            return;
        }
        if (!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,15}$", pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_right_pwd);
            return;
        }
        if (!isSelected) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_agree_to_the_terms_of_use);
            return;
        }
        showLoading();
        MailRegisterBeanReq req = new MailRegisterBeanReq();
        req.setName(mail);
        req.setTokens(tokens);
        req.setPassword(pwd);
        Observable<MailRegisterBeanRsp> observable = HttpRequest.getInstance().register(req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<MailRegisterBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull MailRegisterBeanRsp mailRegisterBeanRsp) {
                dismissLoading();
                if (TextUtils.isEmpty(mailRegisterBeanRsp.getCode())) {
                    Timber.e("register mailRegisterBeanRsp.getCode() is null");
                    return;
                }
                // TODO: 2021/2/2 204,405,435,445 对应的提示语
                if (!mailRegisterBeanRsp.getCode().equals("200")) {
                    Timber.e("register code: %1s, msg: %2s",
                            mailRegisterBeanRsp.getCode(),
                            mailRegisterBeanRsp.getMsg());
                    if (mailRegisterBeanRsp.getMsg() != null) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(mailRegisterBeanRsp.getMsg());
                    }
                    return;
                }
                addUserToLocal(mail);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_register_success);
                // 注册成功, 然后登录再跳转
                login(mail, pwd);
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

    private final CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            String value = String.valueOf((int) (millisUntilFinished / 1000));
            mTvGetCode.setText(value);
        }

        @Override
        public void onFinish() {
            isCountdown = false;
            mTvGetCode.setText(getString(R.string.get_code));
        }
    };

    private void addUserToLocal(String mail) {
        User user = new User();
        user.setMail(mail);
        AppDatabase.getInstance(this).userDao().insert(user);
    }

    private int loginCount = 3;

    private void login(@NotNull String mail, @NotNull String pwd) {
        if (!checkNetConnectFail()) {
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
                if (!mailLoginBeanRsp.getCode().equals("200")) {
                    // TODO: 2021/1/26 获取弹出错误的信息
                    Timber.e("processLoginRsp 登录请求错误了！ code : %1s, msg: %2s",
                            mailLoginBeanRsp.getCode(), mailLoginBeanRsp.getMsg());
                    tryLogin(mail, pwd);
                    return;
                }
                if (mailLoginBeanRsp.getData() == null) {
                    Timber.e("processLoginRsp mailLoginBeanRsp.getData() == null");
                    tryLogin(mail, pwd);
                    return;
                }
                ThreadUtils.getSinglePool().execute(() -> {
                    updateUser(mail, mailLoginBeanRsp.getData());
                    Timber.d("processLoginRsp 登录成功，token: %1s\n userId: %2s",
                            mailLoginBeanRsp.getData().getToken(), mailLoginBeanRsp.getData().getUid());
                    App.getInstance().setUserBean(mailLoginBeanRsp.getData());
                    saveLoginBeanToLocal(mailLoginBeanRsp);
                    gotoSetNameAct();
                });
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoading();
                tryLogin(mail, pwd);
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void tryLogin(@NotNull String mail, @NotNull String pwd) {
        if (loginCount <= 0) {
            gotoLoginAct();
        } else {
            loginCount--;
            login(mail, pwd);
        }
    }

    private void gotoSetNameAct() {
        runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(RegisterActivity.this, RegisterInputNameActivity.class);
            startActivity(intent);
        }, 50));
    }

    private void gotoLoginAct() {
        runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 50));
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
            AppDatabase.getInstance(this).userDao().insert(user);
        } else {
            user.setFirstName(rsp.getFirstName());
            user.setLastName(rsp.getLastName());
            user.setRegisterTime(TimeUtils.string2Millis(rsp.getInsertTime()) / 1000);
            AppDatabase.getInstance(this).userDao().update(user);
        }
    }

}
