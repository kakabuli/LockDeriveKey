package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        applyDebouncingClickListener(findViewById(R.id.btnComplete));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnComplete) {
            updateFirstLastName();
        }
    }

    private void updateFirstLastName() {
        String firstName = etFirstName.getText().toString().trim();
        if(TextUtils.isEmpty(firstName)) {
            // TODO: 2021/3/13 抽离文字
            ToastUtils.showShort("Please input your first name!");
            return;
        }
        String lastName = etLastName.getText().toString().trim();
        if(TextUtils.isEmpty(lastName)) {
            ToastUtils.showShort("Please input your last name!");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("updateFirstLastName App.getInstance().getUserBean() == null");
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
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                ToastUtils.showShort("Success");
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
