package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
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
 * desc   : 添加新密码名称
 */
public class AddNewPwdNameActivity extends BaseActivity {

    private String mEsn;
    private int mPwdNum = -1;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_NUM)) {
            mPwdNum = intent.getIntExtra(Constant.PWD_NUM, -1);
        }
        if(mPwdNum == -1) {
            finish();
            return;
        }
        if(intent.hasExtra(Constant.LOCK_ESN)) {
            mEsn = intent.getStringExtra(Constant.LOCK_ESN);
        }
        if(TextUtils.isEmpty(mEsn)) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_new_pwd_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        applyDebouncingClickListener(findViewById(R.id.btnComplete), findViewById(R.id.tvAddNextTime));
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {
        App.getInstance().finishPreActivities();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnComplete) {
            sendPwdDataToServiceAndLocal();
            return;
        }
        if(view.getId() == R.id.tvAddNextTime) {
            finish();
        }
    }

    private void sendPwdDataToServiceAndLocal() {
        EditText etPwdName = findViewById(R.id.etPwdName);
        String pwdName = etPwdName.getText().toString().trim();
        if(TextUtils.isEmpty(pwdName)) {
            // TODO: 2021/1/30 修改提示语
            ToastUtils.showShort("Please Input Password Name!");
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
        req.setNum(mPwdNum);
        // TODO: 2021/4/8 后面是否需要修改 要与服务器校对，因为服务器MQTT与蓝牙相关字段没有统一
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
                        App.getInstance().logout(true, AddNewPwdNameActivity.this);
                        return;
                    }
                    String msg = changeKeyNickBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", changeKeyNickBeanRsp.getCode(), msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
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
        // TODO: 2021/2/26 文字后期要改
        runOnUiThread(() -> ToastUtils.showShort("Setting failed"));

    }

}
