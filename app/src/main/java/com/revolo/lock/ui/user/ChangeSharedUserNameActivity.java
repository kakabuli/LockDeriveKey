package com.revolo.lock.ui.user;

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
import com.revolo.lock.bean.request.UpdateSharedUserNickNameBeanReq;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.UpdateSharedUserNickNameBeanRsp;
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
 * desc   : 修改已分享用户的名字
 */
public class ChangeSharedUserNameActivity extends BaseActivity {

    private GetAllSharedUserFromAdminUserBeanRsp.DataBean mSharedUserData;
    private EditText etName;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.SHARED_USER_DATA)) {
            mSharedUserData = intent.getParcelableExtra(Constant.SHARED_USER_DATA);
        }
        if(mSharedUserData == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_change_shared_user_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_change_the_name));
        etName = findViewById(R.id.etName);
        applyDebouncingClickListener(findViewById(R.id.btnComplete));
        initLoading("Updating...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnComplete) {
            updateSharedUserName();
        }
    }

    private void updateSharedUserName() {
        if(!checkNetConnectFail()) {
            return;
        }
        String name = etName.getText().toString().trim();
        if(TextUtils.isEmpty(name)) {
            // TODO: 2021/3/14 修改提示语
            ToastUtils.showShort("Please input the new name");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("updateSharedUserName token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("updateSharedUserName uid is empty");
            return;
        }
        UpdateSharedUserNickNameBeanReq req = new UpdateSharedUserNickNameBeanReq();
        req.setNickname(name);
        req.setShareId(mSharedUserData.get_id());
        req.setUid(uid);
        showLoading();
        Observable<UpdateSharedUserNickNameBeanRsp> observable = HttpRequest.getInstance().updateSharedUserNickName(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateSharedUserNickNameBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull UpdateSharedUserNickNameBeanRsp updateSharedUserNickNameBeanRsp) {
                dismissLoading();
                String code = updateSharedUserNickNameBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("updateSharedUserName code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, ChangeSharedUserNameActivity.this);
                        return;
                    }
                    String msg = updateSharedUserNickNameBeanRsp.getMsg();
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    Timber.e("updateSharedUserName code: %1s, msg: %2s", code, msg);
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
