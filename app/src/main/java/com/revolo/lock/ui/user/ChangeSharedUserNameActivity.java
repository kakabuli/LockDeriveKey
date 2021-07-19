package com.revolo.lock.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
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

    private EditText etFirstName, etLastName;
    private String mShareUserUId, mShareFirstName, mShareLastName;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.SHARE_USER_DATA)) {
            mShareUserUId = intent.getStringExtra(Constant.SHARE_USER_DATA);
        }
        if (intent.hasExtra(Constant.SHARE_USER_FIRST_NAME)) {
            mShareFirstName = intent.getStringExtra(Constant.SHARE_USER_FIRST_NAME);
        }
        if (intent.hasExtra(Constant.SHARE_USER_LAST_NAME)) {
            mShareLastName = intent.getStringExtra(Constant.SHARE_USER_LAST_NAME);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_change_shared_user_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_change_the_name));
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            updateSharedUserName();
        });
        initLoading(getString(R.string.t_load_content_updating));
        etFirstName.setText(TextUtils.isEmpty(mShareFirstName) ? "" : mShareFirstName);
        etLastName.setText(TextUtils.isEmpty(mShareLastName) ? "" : mShareLastName);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void updateSharedUserName() {
        if (!checkNetConnectFail()) {
            return;
        }
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_first_name);
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_last_name);
            return;
        }
        if ((firstName.length() < 2 || firstName.length() > 30) || (lastName.length() < 2 || lastName.length() > 30)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_modify_user_name_length);
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("removeUser App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("removeUser token is empty");
            return;
        }
        UpdateSharedUserNickNameBeanReq req = new UpdateSharedUserNickNameBeanReq();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setShareUId(mShareUserUId);
        req.setAdminUId(App.getInstance().getUserBean().getUid());
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
                if (TextUtils.isEmpty(code)) {
                    Timber.e("updateSharedUserName code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ChangeSharedUserNameActivity.this);
                        return;
                    }
                    String msg = updateSharedUserNickNameBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("updateSharedUserName code: %1s, msg: %2s", code, msg);
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
                Intent intent = new Intent();
                intent.putExtra(Constant.SHARE_USER_FIRST_NAME, firstName);
                intent.putExtra(Constant.SHARE_USER_LAST_NAME, lastName);
                setResult(Activity.RESULT_OK, intent);
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
