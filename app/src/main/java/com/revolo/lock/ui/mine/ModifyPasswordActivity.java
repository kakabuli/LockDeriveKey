package com.revolo.lock.ui.mine;

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
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.ChangeUserPwdBeanReq;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.respone.ChangeUserPwdBeanRsp;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改密码
 */
public class ModifyPasswordActivity extends BaseActivity {

    private EditText mEtOldPwd, mEtPwd, mEtVerification;
    private ImageView mIvOldPwdEye, mIvEye;
    private TextView mTvGetCode;

    private boolean isShowOldPwd = false;
    private boolean isShowPwd = false;
    private boolean isCountdown = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_modify_password;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_modify_pwd));
        mEtOldPwd = findViewById(R.id.etOldPwd);
        mEtPwd = findViewById(R.id.etPwd);
        mIvOldPwdEye = findViewById(R.id.ivOldPwdEye);
        mIvEye = findViewById(R.id.ivEye);
        mEtVerification = findViewById(R.id.etVerification);
        mTvGetCode = findViewById(R.id.tvGetCode);
        applyDebouncingClickListener(mIvOldPwdEye, mIvEye, mTvGetCode, findViewById(R.id.btnComplete));
        initLoading("Updating...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivEye) {
            mIvEye.setImageResource(isShowPwd?R.drawable.ic_login_icon_display:R.drawable.ic_login_icon_hide);
            mEtPwd.setInputType(isShowPwd?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowPwd = !isShowPwd;
            return;
        }
        if(view.getId() == R.id.ivOldPwdEye) {
            mIvOldPwdEye.setImageResource(isShowPwd?R.drawable.ic_login_icon_display:R.drawable.ic_login_icon_hide);
            mEtOldPwd.setInputType(isShowOldPwd?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowOldPwd = !isShowOldPwd;
            return;
        }
//        if(view.getId() == R.id.tvGetCode) {
//            if(!isCountdown) {
//                getCode();
//            }
//            return;
//        }
        if(view.getId() == R.id.btnComplete) {
            changeUserPwd();
        }
    }

    private void changeUserPwd() {
        String oldPwd = mEtOldPwd.getText().toString().trim();
        if(TextUtils.isEmpty(oldPwd)) {
            ToastUtils.showShort("Please input old password");
            return;
        }
        if(!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", oldPwd)) {
            ToastUtils.showShort("Please input right old password");
            return;
        }
        String pwd = mEtPwd.getText().toString().trim();
        if(TextUtils.isEmpty(pwd)) {
            ToastUtils.showShort("Please input new password");
            return;
        }
        if(!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", pwd)) {
            ToastUtils.showShort("Please input right new password");
            return;
        }
//        String code = etVerification.getText().toString().trim();
//        if(TextUtils.isEmpty(code)) {
//            ToastUtils.showShort("Please input Verification");
//            return;
//        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("changeUserPwd App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("changeUserPwd token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("changeUserPwd uid is empty");
            return;
        }
        showLoading();
        ChangeUserPwdBeanReq req = new ChangeUserPwdBeanReq();
        req.setNewpwd(pwd);
        req.setOldpwd(oldPwd);
        req.setUid(uid);
        Observable<ChangeUserPwdBeanRsp> observable = HttpRequest.getInstance().changeUserPwd(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<ChangeUserPwdBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ChangeUserPwdBeanRsp changeUserPwdBeanRsp) {
                dismissLoading();
                String code = changeUserPwdBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("changeUserPwd code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    Timber.e("changeUserPwd code: %1s, msg: %2s", code, changeUserPwdBeanRsp.getMsg());
                    return;
                }
                ToastUtils.showShort("Update password success");
                finish();
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
