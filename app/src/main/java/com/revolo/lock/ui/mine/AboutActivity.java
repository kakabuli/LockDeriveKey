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
import com.revolo.lock.bean.respone.LogoutBeanRsp;
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
    private RelativeLayout testService;
    private TextView testView;

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
        testService = findViewById(R.id.test_update_service);
        testView=findViewById(R.id.test_update_service_curr);
        testService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
        updateTest();
    }

    private void updateTest() {
        if ("".equals(SPUtils.getInstance("test").getString("test"))) {
            testView.setText("249");
        } else {
            testView.setText("248");
        }
    }


    private void showLogoutDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage("切换服务器地址");
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            logout();
        });
        dialog.show();
    }

    private void logout() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }
        showLoading("Logging out...");
        Observable<LogoutBeanRsp> observable = HttpRequest.getInstance().logout(token);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LogoutBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LogoutBeanRsp logoutBeanRsp) {
                dismissLoading();
                String code = logoutBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, AboutActivity.this);
                        return;
                    }
                    String msg = logoutBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                        return;
                    }
                }

                //清理mqtt连接
                //关闭MQTT
                MQTTManager.getInstance().mqttDisconnect();

                User user = App.getInstance().getUser();
                AppDatabase.getInstance(getApplicationContext()).userDao().delete(user);
                App.getInstance().getUserBean().setToken(""); // 清空token
                SPUtils.getInstance(REVOLO_SP).put(Constant.USER_LOGIN_INFO, ""); // 清空登录信息
                //清理设备信息
                App.getInstance().removeDeviceList();
                if ("".equals(SPUtils.getInstance("test").getString("test"))) {
                    SPUtils.getInstance("test").put("test", "248");
                } else {
                    SPUtils.getInstance("test").put("test", "");
                }
                startActivity(new Intent(AboutActivity.this, LoginActivity.class).putExtra("logout", true));
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


    @Override
    public void doBusiness() {
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.about_ver, AppUtils.getAppVersionName()).replace("v", "V"));

        TextView tvVersionNew = findViewById(R.id.tv_version_new);
        tvVersionNew.setText(getString(R.string.about_ver, AppUtils.getAppVersionName()).replace("v", "V"));
        TextView tvContact = findViewById(R.id.tvContact);
        // TODO: 2021/3/8 后期从服务器获取
        tvContact.setText("service@irevolo.com");
        applyDebouncingClickListener(findViewById(R.id.clPrivacyAgreement), findViewById(R.id.clVersionUpdate));
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
            launchAppDetail("com.revolo.lock", "com.android.vending");
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
}
