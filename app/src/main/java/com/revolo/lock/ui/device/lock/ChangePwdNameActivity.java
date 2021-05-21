package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.bean.request.ChangeKeyNickBeanReq;
import com.revolo.lock.bean.respone.ChangeKeyNickBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改密码名称
 */
public class ChangePwdNameActivity extends BaseActivity {

    private String mEsn;
    private DevicePwdBean mDevicePwdBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_DETAIL)) {
            mDevicePwdBean = intent.getParcelableExtra(Constant.PWD_DETAIL);
        }
        if(mDevicePwdBean == null) {
            finish();
            return;
        }
        if(intent.hasExtra(Constant.LOCK_ESN)) {
            mEsn = intent.getStringExtra(Constant.LOCK_ESN);
            Timber.d("initData Device Esn: %1s", mEsn);
        }
        if(TextUtils.isEmpty(mEsn)) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_change_pwd_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_change_the_name));
        applyDebouncingClickListener(findViewById(R.id.btnComplete));
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnComplete) {
            sendPwdDataToServiceAndLocal();
        }
    }

    private void sendPwdDataToServiceAndLocal() {
        if(!checkNetConnectFail()) {
            return;
        }
        EditText etPwdName = findViewById(R.id.etPwdName);
        String pwdName = etPwdName.getText().toString().trim();
        if(TextUtils.isEmpty(pwdName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_pwd_name);
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            showAddFail();
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            showAddFail();
            return;
        }

        showLoading();
        ChangeKeyNickBeanReq req = new ChangeKeyNickBeanReq();
        req.setNickName(pwdName);
        req.setNum(mDevicePwdBean.getPwdNum());
        req.setPwdType(1);
        req.setSn(mEsn);
        req.setUid(uid);
        String token = App.getInstance().getUserBean().getToken();
        Observable<ChangeKeyNickBeanRsp> observable = HttpRequest.getInstance().changeKeyNickName(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<ChangeKeyNickBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ChangeKeyNickBeanRsp changeKeyNickBeanRsp) {
                dismissLoading();
                String code = changeKeyNickBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("changeKeyNickBeanRsp.getCode() is Empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, ChangePwdNameActivity.this);
                        return;
                    }
                    String msg = changeKeyNickBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", changeKeyNickBeanRsp.getCode(), msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
                finishThisAct();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
                dismissLoading();
                showAddFail();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    private void finishThisAct() {
        runOnUiThread(this::finish);
    }

    private void showAddFail() {
        runOnUiThread(() -> ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_setting_fail));

    }

}
