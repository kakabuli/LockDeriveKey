package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateUserFirstLastNameBeanReq;
import com.revolo.lock.bean.respone.UpdateUserFirstLastNameBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改用户名称
 */
public class ModifyUserNameActivity extends BaseActivity {

    private EditText etFirstName, etLastName;
    private User mUser;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_modify_user_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_modify_user_name));
        mUser = App.getInstance().getUser();
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        applyDebouncingClickListener(findViewById(R.id.btnComplete));

        if (mUser != null) {
            etFirstName.setText(mUser.getFirstName());
            etLastName.setText(mUser.getLastName());
        }
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
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnComplete) {
            updateFirstLastName();
        }
    }

    private void updateFirstLastName() {
        if (!checkNetConnectFail()) {
            return;
        }
        String firstName = etFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_first_name);
            return;
        }
        String lastName = etLastName.getText().toString().trim();
        if (TextUtils.isEmpty(lastName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_last_name);
            return;
        }
        if ((firstName.length() < 2 || firstName.length() > 30) || (lastName.length() < 2 || lastName.length() > 30)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.tip_modify_user_name_length);
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("updateFirstLastName App.getInstance().getUserBean() == null");
            return;
        }

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateFirstLastName token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("updateFirstLastName uid is empty");
            return;
        }
        UpdateUserFirstLastNameBeanReq req = new UpdateUserFirstLastNameBeanReq();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setUid(uid);
        showLoading();
        Observable<UpdateUserFirstLastNameBeanRsp> observable = HttpRequest.getInstance().updateUserFirstLastName(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateUserFirstLastNameBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull UpdateUserFirstLastNameBeanRsp updateUserFirstLastNameBeanRsp) {
                dismissLoading();
                String code = updateUserFirstLastNameBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ModifyUserNameActivity.this);
                        return;
                    }
                    String msg = updateUserFirstLastNameBeanRsp.getMsg();
                    Timber.e("updateFirstLastName code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
                App.getInstance().getUser().setFirstName(firstName);
                App.getInstance().getUser().setLastName(lastName);
                AppDatabase.getInstance(getApplicationContext()).userDao().update(App.getInstance().getUser());
                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 50);
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
