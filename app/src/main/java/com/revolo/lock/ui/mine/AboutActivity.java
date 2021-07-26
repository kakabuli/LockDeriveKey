package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GetVersionBeanReq;
import com.revolo.lock.bean.respone.GetVersionBeanRsp;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.mqtt.MQTTManager;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 关于页面
 */
public class AboutActivity extends BaseActivity {

    private View vMark;
    private boolean isNewVersion = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_about;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_about));
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.about_ver, AppUtils.getAppVersionName()).replace("v", "V"));
        vMark = findViewById(R.id.v_mark);
        TextView tvVersionNew = findViewById(R.id.tv_version_new);
        tvVersionNew.setText(getString(R.string.about_ver, AppUtils.getAppVersionName()).replace("v", "V"));
        TextView tvContact = findViewById(R.id.tvContact);
        // TODO: 2021/3/8 后期从服务器获取
        tvContact.setText("support@irevolo.com");
        applyDebouncingClickListener(findViewById(R.id.clPrivacyAgreement), findViewById(R.id.clVersionUpdate));
        isNewVersion = false;
        getServerAppVersion();

    }

    @Override
    public void doBusiness() {

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
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.clVersionUpdate) {
            if (isNewVersion) launchAppDetail("com.revolo.lock", "com.android.vending");
        } else if (view.getId() == R.id.clPrivacyAgreement) {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        }
    }

    //参数名：app包名以及google play包名。

    public void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getServerAppVersion() {
        if (!checkNetConnectFail()) {
            return;
        }
        MailLoginBeanRsp.DataBean userBean = App.getInstance().getUserBean();
        if (userBean == null) {
            return;
        }

        String token = userBean.getToken();
        String uid = userBean.getUid();
        GetVersionBeanReq req = new GetVersionBeanReq();
        req.setUid(uid);
        Observable<GetVersionBeanRsp> version = HttpRequest.getInstance().getVersion(token, req);
        ObservableDecorator.decorate(version).safeSubscribe(new Observer<GetVersionBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull GetVersionBeanRsp getVersionBeanRsp) {
                if (getVersionBeanRsp.getCode().equals("200")) {
                    if (getVersionBeanRsp.getData() != null) {
                        String appVersions = getVersionBeanRsp.getData().getAppVersions();
                        if (appVersions.equals(AppUtils.getAppVersionName())) { // 版本号不一致
                            vMark.setVisibility(View.GONE);
                            isNewVersion = false;
                        } else {
                            vMark.setVisibility(View.VISIBLE);
                            isNewVersion = true;
                        }
                    }
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                isNewVersion = false;
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
