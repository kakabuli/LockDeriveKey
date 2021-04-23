package com.revolo.lock.ui.sign;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.ForgotPwdBeanReq;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.respone.ForgotPwdRsp;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.Constant.USER_MAIL;

/**
 * author : Jack
 * time   : 2020/12/24
 * E-mail : wengmaowei@kaadas.com
 * desc   : forget the pwd
 */
public class ForgetThePwdActivity extends BaseActivity {

    private boolean isShowPwd = true;
    private boolean isCountdown = false;
    private TextView mTvGetCode;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_forget_the_pwd;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_forget_the_pwd));

        mTvGetCode = findViewById(R.id.tvGetCode);
        applyDebouncingClickListener(findViewById(R.id.btnDone),
                findViewById(R.id.ivEye),
                mTvGetCode);
        String mail = SPUtils.getInstance(REVOLO_SP).getString(USER_MAIL);
        if(!TextUtils.isEmpty(mail)) {
            EditText etEmail = findViewById(R.id.etEmail);
            etEmail.setText(mail);
        }
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnDone) {
            forgetPwd();
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
        if(view.getId() == R.id.tvGetCode) {
            if(!isCountdown) {
                getCode();
            }
        }
    }

    private void getCode() {
        if(!checkNetConnectFail()) {
            return;
        }
        String mail = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        if(TextUtils.isEmpty(mail)) {
            ToastUtils.showShort(R.string.err_tip_please_input_email);
            return;
        }
        if(!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort(R.string.t_please_input_right_mail_address);
            return;
        }
        showLoading();
        GetCodeBeanReq req = new GetCodeBeanReq();
        req.setMail(mail);
        req.setWorld(2);
        Observable<GetCodeBeanRsp> observable = HttpRequest.getInstance().getCode(req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetCodeBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetCodeBeanRsp getCodeBeanRsp) {
                dismissLoading();
                if(TextUtils.isEmpty(getCodeBeanRsp.getCode())) {
                    Timber.e("getCode getCodeBeanRsp.getCode() is null");
                    ToastUtils.showShort(R.string.t_fail);
                    return;
                }
                if(!getCodeBeanRsp.getCode().equals("200")) {
                    Timber.e("getCode code: %1s, msg: %2s",
                            getCodeBeanRsp.getCode(),
                            getCodeBeanRsp.getMsg());
                    String msg = getCodeBeanRsp.getMsg();
                    if(TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(R.string.t_fail);
                    } else {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                isCountdown = true;
                mCountDownTimer.start();
                ToastUtils.showShort(R.string.t_success);
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

    private void forgetPwd() {
        if(!checkNetConnectFail()) {
            return;
        }
        String mail = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        if(TextUtils.isEmpty(mail)) {
            ToastUtils.showShort(R.string.err_tip_please_input_email);
            return;
        }
        if(!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort(R.string.t_please_input_right_mail_address);
            return;
        }
        String tokens = ((EditText) findViewById(R.id.etCode)).getText().toString().trim();
        if(TextUtils.isEmpty(tokens)) {
            ToastUtils.showShort(R.string.err_tip_please_input_verification_code);
            return;
        }
        String pwd = ((EditText) findViewById(R.id.etPwd)).getText().toString().trim();
        if(TextUtils.isEmpty(pwd)) {
            ToastUtils.showShort(R.string.t_please_input_pwd);
            return;
        }
        if(!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", pwd)) {
            ToastUtils.showShort(R.string.t_please_input_right_pwd);
            return;
        }
        showLoading();
        ForgotPwdBeanReq req = new ForgotPwdBeanReq();
        req.setName(mail);
        req.setPwd(pwd);
        req.setTokens(tokens);
        req.setType(2);
        Observable<ForgotPwdRsp> observable = HttpRequest.getInstance().forgotPwd(req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<ForgotPwdRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ForgotPwdRsp forgotPwdRsp) {
                dismissLoading();
                if(TextUtils.isEmpty(forgotPwdRsp.getCode())) {
                    ToastUtils.showShort(R.string.t_fail);
                    return;
                }
                if(!forgotPwdRsp.getCode().equals("200")) {
                    Timber.e("forgetPwd code: %1s, msg: %2s", forgotPwdRsp.getCode(), forgotPwdRsp.getMsg());
                    String msg = forgotPwdRsp.getMsg();
                    if(TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(R.string.t_fail);
                    } else {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                ToastUtils.showShort(R.string.t_success);
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
}
