package com.revolo.lock.ui.sign;

import android.content.Intent;
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

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class RegisterInputNameActivity extends BaseActivity {

    private EditText etFirstName, etLastName;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register_input_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        applyDebouncingClickListener(findViewById(R.id.btnNext));
        initLoading(getString(R.string.t_load_content_updating));
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
        if(view.getId() == R.id.btnNext) {
            updateFirstLastName();
        }
    }

    private void updateFirstLastName() {
        if(!checkNetConnectFail()) {
            return;
        }
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        if(TextUtils.isEmpty(firstName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_first_name);
            return;
        }
        if(TextUtils.isEmpty(lastName)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_your_last_name);
            return;
        }
        if(firstName.length()>30||firstName.length()<2||lastName.length()>30||lastName.length()<2){
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.login_name_len_max_hint);
            return;
        }

        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("updateFirstLastName token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
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
                if(TextUtils.isEmpty(code)) {
                    return;
                }
                if(!code.equals("200")) {
                    String msg = updateUserFirstLastNameBeanRsp.getMsg();
                    Timber.e("updateFirstLastName code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                saveNameToLocal(firstName, lastName);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(RegisterInputNameActivity.this, RegisterAddAvatarActivity.class);
                    startActivity(intent);
                }, 50);
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

    private void saveNameToLocal(@NotNull String firstName, @NotNull String lastName) {
        if(App.getInstance().getUserBean() == null) {
            return;
        }
        App.getInstance().getUserBean().setFirstName(firstName);
        App.getInstance().getUserBean().setLastName(lastName);
        if(App.getInstance().getUser() != null) {
            App.getInstance().getUser().setFirstName(firstName);
            App.getInstance().getUser().setLastName(lastName);
            AppDatabase.getInstance(this).userDao().update(App.getInstance().getUser());
        }
    }

}
