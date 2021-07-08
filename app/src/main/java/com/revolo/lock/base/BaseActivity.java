package com.revolo.lock.base;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.manager.LockConnected;
import com.revolo.lock.shulan.KeepAliveManager;
import com.revolo.lock.shulan.config.ForegroundNotification;
import com.revolo.lock.shulan.config.RunMode;
import com.revolo.lock.ui.TitleBar;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.PING_RESULT;
import static com.revolo.lock.Constant.RECEIVE_ACTION_NETWORKS;
import static com.revolo.lock.Constant.isRegisterReceiver;

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

    public static final int VERIFICATION_CODE_TIME = 0xf01;

    private final View.OnClickListener mClickListener = this::onDebouncingClick;
    private static TitleBar mTitleBar;

    public View mContentView;
    public Activity mActivity;
    private CustomerLoadingDialog mLoadingDialog;
    public boolean isShowNetState = true;
    private BluetoothAdapter mBluetoothAdapter;

    public static int mVerificationCodeTime = 60;

    protected static final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == VERIFICATION_CODE_TIME) {
                if (mVerificationCodeTime > 0) {
                    mVerificationCodeTime--;
                    sendEmptyMessageDelayed(VERIFICATION_CODE_TIME, 1000);
                    Timber.d("###########################   mVerificationCodeTime = " + mVerificationCodeTime + "   ###########################");
                    Constant.isVerificationCodeTime = true;
                    Constant.verificationCodeTimeCount = mVerificationCodeTime;
                } else {
                    Constant.isVerificationCodeTime = false;
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().equals(RECEIVE_ACTION_NETWORKS)) {
                    boolean pingResult = intent.getBooleanExtra(PING_RESULT, true);
                    noteNetworks(pingResult);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    // 屏幕打开了
                    Timber.d("###########################   screen on   ###########################");
                    boolean pingResult = intent.getBooleanExtra(PING_RESULT, true);
                    noteNetworks(pingResult);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        isShowNetState = true;
        LockAppManager.getAppManager().addActivity(this);
        mActivity = this;
        super.onCreate(savedInstanceState);
        initData(getIntent().getExtras());
        setContentView();
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        LockConnected bleConnected = new LockConnected();
        bleConnected.setConnectType(0);
        EventBus.getDefault().post(bleConnected);

        initView(savedInstanceState, mContentView);

//        startKeepAlive();

        if (!isRegisterReceiver) {  // 判断广播是否注册
            Timber.d("###########################   广播注册成功   ###########################");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RECEIVE_ACTION_NETWORKS);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(mReceiver, intentFilter);
            isRegisterReceiver = true;
        }
    }

    public void noteNetworks(boolean pingResult) {
        Timber.d("###########################   pingResult = " + pingResult + "   ####################################");
        if (mTitleBar != null) {
            mTitleBar.setNetError(pingResult);
        }
        mContentView.postInvalidate(); // 刷新页面
    }

    public void onRegisterEventBus() {
        boolean registered = EventBus.getDefault().isRegistered(this);
        if (!registered) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBusiness();
        if (mTitleBar != null) {
            mTitleBar.setNetError(Constant.pingResult);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                dismissLoading();
            }
        }
        LockAppManager.getAppManager().finishActivity(this);
        boolean registered = EventBus.getDefault().isRegistered(this);
        if (registered) {
            EventBus.getDefault().unregister(this);
        }
        if (mReceiver != null && LockAppManager.getAppManager().getActivitySize() == 0) {
            Timber.d("####################   注销广播   ########################");
            unregisterReceiver(mReceiver);
            isRegisterReceiver = false;
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
                        (context, intent) -> {
                            Timber.e("JOB-->  foregroundNotificationClick");
//                                stopKeepAlive();
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
        mTitleBar = new TitleBar(mContentView).setTitle(title).useCommonLeft(v -> finish());
        return mTitleBar;
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
            if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
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
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.connect_net_fail);
        }
        return NetworkUtils.isConnected();
    }
}
