package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
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

    private EditText mEtOldPwd, mEtPwd;
    private ImageView mIvOldPwdEye, mIvEye;
    private TextView mTvGetCode;

    private boolean isShowOldPwd = false;
    private boolean isShowPwd = false;
    private boolean isCountdown = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        mTvGetCode = findViewById(R.id.tvGetCode);
        applyDebouncingClickListener(mIvOldPwdEye, mIvEye, mTvGetCode, findViewById(R.id.btnComplete));
        initLoading(getString(R.string.t_load_content_updating));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.ivEye) {
            mIvEye.setImageResource(isShowPwd ? R.drawable.ic_login_icon_display_blue : R.drawable.ic_login_icon_hide_blue);
            mEtPwd.setInputType(isShowPwd ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowPwd = !isShowPwd;
            String trim = mEtPwd.getText().toString().trim();
            if (!TextUtils.isEmpty(trim)) {
                mEtPwd.setSelection(trim.length());
            }
            return;
        }
        if (view.getId() == R.id.ivOldPwdEye) {
            mIvOldPwdEye.setImageResource(isShowOldPwd ? R.drawable.ic_login_icon_display_blue : R.drawable.ic_login_icon_hide_blue);
            mEtOldPwd.setInputType(isShowOldPwd ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            isShowOldPwd = !isShowOldPwd;
            String trim = mEtOldPwd.getText().toString().trim();
            if (!TextUtils.isEmpty(trim)) {
                mEtOldPwd.setSelection(trim.length());
            }
            return;
        }
//        if(view.getId() == R.id.tvGetCode) {
//            if(!isCountdown) {
//                getCode();
//            }
//            return;
//        }
        if (view.getId() == R.id.btnComplete) {
            changeUserPwd();
        }
    }

    private void changeUserPwd() {
        if (!checkNetConnectFail()) {
            return;
        }
        String oldPwd = mEtOldPwd.getText().toString().trim();
        if (TextUtils.isEmpty(oldPwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_old_pwd);
            return;
        }
        if (!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,15}$", oldPwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_you_can_set_8_15_valid_pwd);
            return;
        }
        String pwd = mEtPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_new_pwd);
            return;
        }
        if (!RegexUtils.isMatch("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,15}$", pwd)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_you_can_set_8_15_valid_pwd);
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("changeUserPwd App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("changeUserPwd token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
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
                if (TextUtils.isEmpty(code)) {
                    Timber.e("changeUserPwd code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ModifyPasswordActivity.this);
                        return;
                    }
                    String msg = changeUserPwdBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("changeUserPwd code: %1s, msg: %2s", code, changeUserPwdBeanRsp.getMsg());
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_update_pwd_suc);
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
        if (!checkNetConnectFail()) {
            return;
        }
        String mail = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
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
                String code = getCodeBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("getCodeBeanRsp.getCode() is null");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ModifyPasswordActivity.this);
                        return;
                    }
                    String msg = getCodeBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("code: %1s, msg: %2s", code, msg);
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
