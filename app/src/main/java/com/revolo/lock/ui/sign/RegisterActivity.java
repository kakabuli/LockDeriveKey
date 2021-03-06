package com.revolo.lock.ui.sign;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.request.MailRegisterBeanReq;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.util.LinkClickableSpan;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;


/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : 注册页面
 */
public class RegisterActivity extends BaseActivity {

    private boolean isSelected = false;
    private boolean isShowPwd = false;
    private boolean isCountdown = false;

    private TextView mTvGetCode;

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
            // TODO: 2021/1/11 跳转到协议界面
        };
        spannableString.setSpan(span, 0, agreementStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvAgreement.append(getString(R.string.i_agree_to));
        tvAgreement.append(spannableString);

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnStartCreating) {
            register();
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
        if(view.getId() == R.id.ivSelect) {
            ImageView ivSelect = findViewById(R.id.ivSelect);
            isSelected = !isSelected;
            ivSelect.setImageResource(isSelected?R.drawable.ic_sign_in_icon_selected:R.drawable.ic_sign_in_icon_default);
            return;
        }
        if(view.getId() == R.id.tvGetCode) {
            if(!isCountdown) {
                getCode();
            }
        }
    }

    private void getCode() {
        String mail = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        // TODO: 2021/2/2 修正提示语
        if(TextUtils.isEmpty(mail)) {
            ToastUtils.showShort("Please input mail");
            return;
        }
        if(!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort("Please input right mail address");
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
                if(TextUtils.isEmpty(getCodeBeanRsp.getCode())) {
                    Timber.e("getCodeBeanRsp.getCode() is null");
                    return;
                }
                // TODO: 2021/2/2 对应的提示语
                if(!getCodeBeanRsp.getCode().equals("200")) {
                    Timber.e("code: %1s, msg: %2s",
                            getCodeBeanRsp.getCode(),
                            getCodeBeanRsp.getMsg());
                    return;
                }
                // TODO: 2021/2/2 对应的提示语
                ToastUtils.showShort("Success!");
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
        String mail = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        // TODO: 2021/2/2 修正提示语
        if(TextUtils.isEmpty(mail)) {
            ToastUtils.showShort("Please input mail");
            return;
        }
        if(!RegexUtils.isEmail(mail)) {
            ToastUtils.showShort("Please input right mail address");
            return;
        }
        String tokens = ((EditText) findViewById(R.id.etVerification)).getText().toString().trim();
        if(TextUtils.isEmpty(tokens)) {
            ToastUtils.showShort("Please input Verification");
            return;
        }
        String pwd = ((EditText) findViewById(R.id.etPwd)).getText().toString().trim();
        if(TextUtils.isEmpty(pwd)) {
            ToastUtils.showShort("Please input password");
            return;
        }
        if(!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", pwd)) {
            ToastUtils.showShort("Please input right password");
            return;
        }
        // TODO: 2021/2/8 抽离提示语
        if(!isSelected) {
            ToastUtils.showShort("Please agree to the terms of use");
            return;
        }
        // TODO: 2021/2/21 抽离文字
        CustomerLoadingDialog loadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("loading...")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
        loadingDialog.show();
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
                if(loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                if(TextUtils.isEmpty(mailRegisterBeanRsp.getCode())) {
                    Timber.e("register mailRegisterBeanRsp.getCode() is null");
                    return;
                }
                // TODO: 2021/2/2 204,405,435,445 对应的提示语
                if(!mailRegisterBeanRsp.getCode().equals("200")) {
                    Timber.e("register code: %1s, msg: %2s",
                            mailRegisterBeanRsp.getCode(),
                            mailRegisterBeanRsp.getMsg());
                    if(mailRegisterBeanRsp.getMsg() != null) {
                        ToastUtils.showShort(mailRegisterBeanRsp.getMsg());
                    }
                    return;
                }
                addUserToLocal(mail);
                // TODO: 2021/2/8 注册成功
                ToastUtils.showShort("Register Success!");
//                startActivity(new Intent(RegisterActivity.this, RegisterInputNameActivity.class));
                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 50);
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

}
