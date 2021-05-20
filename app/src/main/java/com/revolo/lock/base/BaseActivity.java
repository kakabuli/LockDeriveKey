package com.revolo.lock.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.mqtt.MqttService;
import com.revolo.lock.shulan.KeepAliveManager;
import com.revolo.lock.shulan.config.ForegroundNotification;
import com.revolo.lock.shulan.config.ForegroundNotificationClickListener;
import com.revolo.lock.shulan.config.RunMode;
import com.revolo.lock.ui.TitleBar;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/10/24
 *     desc  : base about activity
 * </pre>
 */
public abstract class BaseActivity extends AppCompatActivity
        implements IBaseView {

    private final View.OnClickListener mClickListener = this::onDebouncingClick;
    public CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    public MqttService mMQttService = App.getInstance().getMQttService();

    public View mContentView;
    public Activity mActivity;
    private CustomerLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
        initData(getIntent().getExtras());
        setContentView();
        if (mMQttService == null) {
            mMQttService = App.getInstance().getMQttService();
        }
        if (mMQttService != null) {
            if (mMQttService.getMqttClient() != null && !mMQttService.getMqttClient().isConnected()) {
                mMQttService.mqttConnection();
            }
        }
        initView(savedInstanceState, mContentView);

        startKeepAlive();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBusiness();
    }

    @Override
    protected void onStop() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                dismissLoading();
            }
        }
        super.onDestroy();
    }

    @Override
    public void setContentView() {
        if (bindLayout() <= 0) return;
        mContentView = LayoutInflater.from(this).inflate(bindLayout(), null);
        setContentView(mContentView);
    }

    /**
     * 启动保活
     */
    private void startKeepAlive() {
        //启动保活服务
        // TODO: 2021/3/30 保活文字
        KeepAliveManager.toKeepAlive(
                getApplication()
                , RunMode.HIGH_POWER_CONSUMPTION, getString(R.string.app_name_notification_title),
                getString(R.string.app_name_notification_content),
                R.mipmap.ic_launcher,
                new ForegroundNotification(
                        //定义前台服务的通知点击事件
                        new ForegroundNotificationClickListener() {
                            @Override
                            public void foregroundNotificationClick(Context context, Intent intent) {
                                Timber.e("JOB-->  foregroundNotificationClick");
//                                stopKeepAlive();
                            }
                        })
        );
    }

    /**
     * 停止保活
     */
    public void stopKeepAlive() {
        KeepAliveManager.stopWork(getApplication());
    }


    public TitleBar useCommonTitleBar(String title) {
        setStatusBarColor(R.color.white);
        return new TitleBar(mContentView).setTitle(title).useCommonLeft(v -> finish());
    }

    public void applyDebouncingClickListener(View... views) {
        ClickUtils.applyGlobalDebouncing(views, mClickListener);
        ClickUtils.applyPressedViewScale(views);
    }

    @Override
    public Resources getResources() {
        return AdaptScreenUtils.adaptWidth(super.getResources(), 375);
    }

    public void setStatusBarColor(@ColorRes int id) {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, id));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void initLoading(String message) {
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    public void showLoading(@NotNull String message) {
        runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
            mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                    .setMessage(message)
                    .setCancelable(true)
                    .setCancelOutside(false)
                    .create();
            mLoadingDialog.show();
        });
    }

    public void showLoading() {
        runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        });
    }

    public void dismissLoading() {
        runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });
    }

    public void toDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public boolean checkNetConnectFail() {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.showShort(R.string.connect_net_fail);
        }
        return NetworkUtils.isConnected();
    }
}
