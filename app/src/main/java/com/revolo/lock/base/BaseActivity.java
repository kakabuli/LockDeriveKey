package com.revolo.lock.base;

import android.app.Activity;
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
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.ui.TitleBar;

import org.jetbrains.annotations.NotNull;

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

    public View     mContentView;
    public Activity mActivity;
    private CustomerLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
        initData(getIntent().getExtras());
        setContentView();
        if(App.getInstance().getMqttService() != null) {
            App.getInstance().getMqttService().mqttConnection();
        }
        initView(savedInstanceState, mContentView);
        doBusiness();
    }

    @Override
    public void setContentView() {
        if (bindLayout() <= 0) return;
        mContentView = LayoutInflater.from(this).inflate(bindLayout(), null);
        setContentView(mContentView);
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
        // TODO: 2021/2/25 抽离文字
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    // TODO: 2021/3/4 修改抽离文字
    public void showLoading(@NotNull String message) {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                if(mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
            // TODO: 2021/2/25 抽离文字
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
            if(mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        });
    }

    public void dismissLoading() {
        runOnUiThread(() -> {
            if(mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        });
    }

}
