package com.revolo.lock.ui.device.lock.setting;

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
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.ChangeDeviceNameBeanReq;
import com.revolo.lock.bean.respone.ChangeDeviceNameBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改锁名称
 */
public class ChangeLockNameActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private EditText etLockName;
    private String mTvName;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_change_lock_name;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        mTvName = getIntent().getStringExtra(Constant.CHANGE_LOCK_NAME);
        useCommonTitleBar(getString(R.string.chang_pwd_name_activity_title));
        etLockName = findViewById(R.id.etLockName);
        if (TextUtils.isEmpty(mTvName)) {
            mTvName = mBleDeviceLocal.getEsn();
            findViewById(R.id.btnCancel).setVisibility(View.VISIBLE);
        } else {
            etLockName.setText(mTvName);
            findViewById(R.id.btnCancel).setVisibility(View.GONE);
        }
        applyDebouncingClickListener(findViewById(R.id.btnComplete), findViewById(R.id.btnCancel));
        initLoading(getString(R.string.t_load_content_setting));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnComplete) {
            changeDeviceName();
        } else if (view.getId() == R.id.btnCancel) {
            finish();
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

    private void changeDeviceName() {
        if (!checkNetConnectFail()) {
            return;
        }
        String name = etLockName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_name);
            return;
        }
        if (name.length() < 2) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_please_input_name_2);
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();

        if (TextUtils.isEmpty(token)) {
            return;
        }
        showLoading();
        ChangeDeviceNameBeanReq req = new ChangeDeviceNameBeanReq();
        req.setLockNickName(name);
        req.setSn(mBleDeviceLocal.getEsn());
        req.setUid(uid);
        Observable<ChangeDeviceNameBeanRsp> observable = HttpRequest.getInstance().changeDeviceNickName(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<ChangeDeviceNameBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ChangeDeviceNameBeanRsp changeDeviceNameBeanRsp) {
                dismissLoading();
                String code = changeDeviceNameBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("changeDeviceNameBeanRsp.getCode() is Empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, ChangeLockNameActivity.this);
                        return;
                    }
                    String msg = changeDeviceNameBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", changeDeviceNameBeanRsp.getCode(), msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                updateNameToLocal(name);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
                new Handler(Looper.getMainLooper()).postDelayed(() -> finishThisAct(), 50);
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

    private void updateNameToLocal(@NotNull String name) {
        mBleDeviceLocal.setName(name);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    private void finishThisAct() {
        runOnUiThread(this::finish);
    }

}
