package com.revolo.lock.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.AcceptShareBeanReq;
import com.revolo.lock.bean.respone.AcceptShareBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/3/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 接受邀请
 */
public class ShareDeviceActivity extends BaseActivity {

    private String mShareKey, mUserName, mLockName;
    private TextView tvShareUser, tvJoinTip;

    @Override
    public void initData(@Nullable Bundle bundle) {
        getShareUserIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getShareUserIntent(getIntent());
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
        return R.layout.activity_share_device;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.share_device));
        tvShareUser = findViewById(R.id.tvShareUser);
        tvJoinTip = findViewById(R.id.tvJoinTip);
        applyDebouncingClickListener(findViewById(R.id.btnAccept));
        initLoading(getString(R.string.t_load_content_accepting));
    }

    @Override
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnAccept) {
            acceptShare();
        }
    }

    private void refreshUI() {
        tvShareUser.setText(getString(R.string.shared_by_user, mUserName));
        tvJoinTip.setText(getString(R.string.join_tip, mUserName, mLockName));
    }

    private void acceptShare() {
        if (!checkNetConnectFail()) {
            return;
        }
        // TODO: 2021/4/23 跳转到登录页面
        if (App.getInstance() == null) {
            Timber.e("acceptShare App.getInstance() == null");
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_sign_in);
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("acceptShare App.getInstance().getUserBean() == null");
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_sign_in);
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("acceptShare uid is empty");
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_sign_in);
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("acceptShare token is empty");
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_sign_in);
            return;
        }

        AcceptShareBeanReq req = new AcceptShareBeanReq();
        req.setShareKey(mShareKey);
        Observable<AcceptShareBeanRsp> observable = HttpRequest.getInstance().acceptShare(token, req);
        // TODO: 2021/3/12 暂时屏蔽
        showLoading();
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<AcceptShareBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull AcceptShareBeanRsp acceptShareBeanRsp) {
                dismissLoading();
                String code = acceptShareBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ShareDeviceActivity.this);
                        return;
                    }
                    String msg = acceptShareBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
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

    private void getShareUserIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = getIntent().getData();

            if (data != null) {
                String shareKey = data.getQueryParameter("shareKey");
                Timber.d("shareKey: %1s", shareKey);
            }
        }
    }

}
