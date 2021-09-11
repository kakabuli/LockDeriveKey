package com.revolo.lock.base;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.bean.NetWorkStateBean;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.manager.LockConnected;
import com.revolo.lock.shulan.KeepAliveManager;
import com.revolo.lock.shulan.config.ForegroundNotification;
import com.revolo.lock.shulan.config.RunMode;
import com.revolo.lock.ui.TitleBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

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
        implements IBaseView, BaseObserver {

    public static final int VERIFICATION_CODE_TIME = 0xf01;

    private final View.OnClickListener mClickListener = this::onDebouncingClick;
    private static TitleBar mTitleBar;

    public View mContentView;
    public Activity mActivity;
    private CustomerLoadingDialog mLoadingDialog;
    public boolean isShowNetState = true;
    private BluetoothAdapter mBluetoothAdapter;
    private SelectDialog OpenBluetoothDialog;

    public static int mVerificationCodeTime = 60;

    protected static final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == VERIFICATION_CODE_TIME) {
                if (mVerificationCodeTime > 0) {
                    mVerificationCodeTime--;
                    sendEmptyMessageDelayed(VERIFICATION_CODE_TIME, 1000);
                    Timber.d("**************************   mVerificationCodeTime = " + mVerificationCodeTime + "   **************************");
                    Constant.isVerificationCodeTime = true;
                    Constant.verificationCodeTimeCount = mVerificationCodeTime;
                } else {
                    Constant.isVerificationCodeTime = false;
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

        LockConnected bleConnected = new LockConnected();
        bleConnected.setConnectType(LocalState.CONNECT_STATE_MQTT);
        EventBus.getDefault().post(bleConnected);

        initView(savedInstanceState, mContentView);

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
        super.onDestroy();
    }

    @Override
    public void setContentView() {
        if (bindLayout() <= 0) return;
        mContentView = LayoutInflater.from(this).inflate(bindLayout(), null);
        setContentView(mContentView);
    }

    /**
     * 是否去开启蓝牙的回调
     */
    public interface checkOpenBluetoothClick {
        void onOpenBluetooth(int type);
    }

    private checkOpenBluetoothClick openBluetoothClick;

    public void setOpenBluetoothClick(checkOpenBluetoothClick openBluetoothClick) {
        this.openBluetoothClick = openBluetoothClick;
    }

    /**
     * 检测当前蓝牙是否开启、并提示用户开启bluetooth
     * @param type  操作类型
     * @return
     */
    public boolean checkIsOpenBluetooth(int type) {
        Timber.e("检测当前手机蓝牙是否开启");
        if (null == mBluetoothAdapter) {
            if (null != App.getInstance().getLockAppService()) {
                mBluetoothAdapter = App.getInstance().getLockAppService().getBluetoothAdapter();
            }
        }
        if (null != mBluetoothAdapter) {
            if (!mBluetoothAdapter.isEnabled()) {
                //蓝牙关闭
                if (null == OpenBluetoothDialog) {
                    OpenBluetoothDialog = new SelectDialog(this);
                    OpenBluetoothDialog.setMessage(getString(R.string.dialog_tip_hint_is_open_bluetooth));
                    OpenBluetoothDialog.setOnCancelClickListener(v -> OpenBluetoothDialog.dismiss());
                    OpenBluetoothDialog.setOnConfirmListener(v -> {
                        OpenBluetoothDialog.dismiss();
                        if (null != openBluetoothClick) {
                            openBluetoothClick.onOpenBluetooth(type);
                        }
                        if (null != mBluetoothAdapter) {
                            mBluetoothAdapter.enable();
                        }
                    });
                }
                if (!OpenBluetoothDialog.isShowing()) {
                    OpenBluetoothDialog.show();
                }
            } else {
                return true;
            }
        }
        return false;
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
    public TitleBar useCommonTitleBar(String title, View.OnClickListener listener) {
        setStatusBarColor(R.color.white);
        mTitleBar = new TitleBar(mContentView).setTitle(title).useCommonLeft(listener);
        return mTitleBar;
    }

    public void applyDebouncingClickListener(View... views) {
        ClickUtils.applyGlobalDebouncing(views, mClickListener);
//        ClickUtils.applyPressedViewScale(views);
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

    /**
     * 设置加载对话框是否因返回键消失
     *
     * @param isDown
     */
    protected void setLoadingDialog(boolean isDown) {
        if (null != mLoadingDialog) {
            mLoadingDialog.setReturnDiss(isDown);
        }
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

    @Override
    public void notifyNetWork(boolean isNetWork) {
        Timber.d("**************************   pingResult = " + isNetWork + "   **************************");
        if (mTitleBar != null) {
            mTitleBar.setNetError(isNetWork);
        }
        NetWorkStateBean bean = new NetWorkStateBean();
        bean.setPingResult(isNetWork);
        EventBus.getDefault().post(bean);
        mContentView.postInvalidate(); // 刷新页面
    }

    public boolean getNetError() {
        if (null == mTitleBar) {
            return true;
        }
        return mTitleBar.getNetError();
    }
}
