package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 选择对应的权限
 */
public class SelectAuthorizedDeviceActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private TextView tvUserName, tvSn, tvUserTip;
    private ImageView mIvGuest, mIvFamily;
    private String mShareUserMail, mShareUserFirstName, mShareUserLastName, uid;

    // TODO: 2021/3/8 后续写成enum
    private int mCurrentUserType = 1;                // 1 Family  2 Guest

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.LOCK_DETAIL)) {
            mBleDeviceLocal = intent.getParcelableExtra(Constant.LOCK_DETAIL);
        }
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_select_authorized_device;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_select_authorized_device));
        tvUserName = findViewById(R.id.tvUserName);
        tvSn = findViewById(R.id.tvSn);
        mIvGuest = findViewById(R.id.ivGuest);
        tvUserTip = findViewById(R.id.tvUserTip);
        mIvFamily = findViewById(R.id.ivFamily);
        findViewById(R.id.clFamily).setOnClickListener(v -> {
            mCurrentUserType = 1;
            mIvGuest.setImageResource(R.drawable.ic_home_password_icon_default);
            mIvFamily.setImageResource(R.drawable.ic_home_password_icon_selected);
        });
        findViewById(R.id.clGuest).setOnClickListener(v -> {
            mCurrentUserType = 2;
            mIvGuest.setImageResource(R.drawable.ic_home_password_icon_selected);
            mIvFamily.setImageResource(R.drawable.ic_home_password_icon_default);
        });
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            share();
        });
        initLoading(getString(R.string.t_load_content_creating));

        mShareUserMail = getIntent().getStringExtra(Constant.SHARE_USER_MAIL);
        mShareUserFirstName = getIntent().getStringExtra(Constant.SHARE_USER_FIRST_NAME);
        mShareUserLastName = getIntent().getStringExtra(Constant.SHARE_USER_LAST_NAME);
        uid = getIntent().getStringExtra(Constant.SHARE_USER_DATA);
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
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void refreshUI() {
        String name = mBleDeviceLocal.getName();
        String esn = mBleDeviceLocal.getEsn();
        String userName = (TextUtils.isEmpty(mShareUserFirstName) ? "" : mShareUserFirstName) + " " + (TextUtils.isEmpty(mShareUserLastName) ? "" : mShareUserLastName);
        tvUserName.setText(userName);
        tvSn.setText(TextUtils.isEmpty(esn) ? "" : getString(R.string.equipment_n_esn, esn));
        tvUserTip.setText(String.format("You will invite %1s users to use the lock", (TextUtils.isEmpty(name) ? (TextUtils.isEmpty(esn) ? "" : esn) : name)));
    }

    private void share() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("share App.getInstance().getUserBean() == null");
            return;
        }
        String adminUid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(adminUid)) {
            Timber.e("share adminUid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("share token is empty");
            return;
        }
        if (TextUtils.isEmpty(uid)) {
            Timber.e("share uid is empty");
            return;
        }
        GainKeyBeanReq req = new GainKeyBeanReq();
        req.setDeviceSN(mBleDeviceLocal.getEsn());
        req.setShareUserType(mCurrentUserType);
        req.setAdminUid(adminUid);
        req.setUid(uid);
        req.setFirstName(mShareUserFirstName);
        req.setLastName(mShareUserLastName);
        req.setShareAccount(mShareUserMail);
        showLoading();
        Observable<GainKeyBeanRsp> observable = HttpRequest.getInstance().gainKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GainKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GainKeyBeanRsp gainKeyBeanRsp) {
                dismissLoading();
                String code = gainKeyBeanRsp.getCode();
                String msg = gainKeyBeanRsp.getMsg();
                if (code.equals("200")) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.tip_content_share_successful));
                    ActivityUtils.finishActivity(SelectAuthorizedDeviceActivity.class);
                    ActivityUtils.finishActivity(AddDeviceForSharedUserActivity.class);
                    ActivityUtils.finishActivity(InviteUsersMailActivity.class);
                } else if (code.equals("444")) {
                    App.getInstance().logout(true, SelectAuthorizedDeviceActivity.this);
                } else {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                }
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

    private void shareUrlToOtherApp(@NotNull String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_share_to));
        intent.putExtra(Intent.EXTRA_TEXT, url);//extraText为文本的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
        startActivity(intent);
    }

}
