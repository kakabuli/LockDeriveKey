package com.revolo.lock.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
        Uri uri = getIntent().getData();
        // TODO: 2021/3/12 弹出对应的提示
        if(uri == null) {
            finish();
            return;
        }
        mShareKey = uri.getQueryParameter("shareKey");
        if(TextUtils.isEmpty(mShareKey)) {
            finish();
            return;
        }
        mUserName = uri.getQueryParameter("userName");
        if(TextUtils.isEmpty(mUserName)) {
            finish();
            return;
        }
        mLockName = uri.getQueryParameter("lockName");
        if(TextUtils.isEmpty(mLockName)) {
            finish();
        }
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
        initLoading("Accepting...");
    }

    @Override
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnAccept) {
            acceptShare();
        }
    }

    private void refreshUI() {
        tvShareUser.setText(getString(R.string.shared_by_user, mUserName));
        tvJoinTip.setText(getString(R.string.join_tip, mUserName, mLockName));
    }

    private void acceptShare() {
        if(App.getInstance() == null) {
            Timber.e("acceptShare App.getInstance() == null");
            ToastUtils.showShort("Please sign in!");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("acceptShare App.getInstance().getUserBean() == null");
            ToastUtils.showShort("Please sign in!");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("acceptShare uid is empty");
            ToastUtils.showShort("Please sign in!");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("acceptShare token is empty");
            ToastUtils.showShort("Please sign in!");
            return;
        }

        AcceptShareBeanReq req = new AcceptShareBeanReq();
        req.setShareKey(mShareKey);
        req.setUid(uid);
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
                if(TextUtils.isEmpty(code)) {
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, ShareDeviceActivity.this);
                        return;
                    }
                    String msg = acceptShareBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                ToastUtils.showShort("Success");
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

}
