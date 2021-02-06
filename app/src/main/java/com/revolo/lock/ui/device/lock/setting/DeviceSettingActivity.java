package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.dialog.UnbindLockDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.widget.iosloading.CustomerLoadingDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备设置
 */
public class DeviceSettingActivity extends BaseActivity {

    private TextView mTvName, mTvWifiName;
    private DeviceUnbindBeanReq mReq;
    private CustomerLoadingDialog mLoadingDialog;


    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.UNBIND_REQ)) {
            mReq = intent.getParcelableExtra(Constant.UNBIND_REQ);
        } else {
            // TODO: 2021/2/6 提示没从上一个页面传递数据过来
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_device_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_setting));
        mTvName = findViewById(R.id.tvName);
        mTvWifiName = findViewById(R.id.tvWifiName);
        applyDebouncingClickListener(mTvName, mTvWifiName,
                findViewById(R.id.clAutoLock), findViewById(R.id.clPrivateMode),
                findViewById(R.id.clDuressCode), findViewById(R.id.clDoorLockInformation),
                findViewById(R.id.clGeoFenceLock), findViewById(R.id.clDoorMagneticSwitch),
                findViewById(R.id.clUnbind));
        // TODO: 2021/1/29 抽离英文
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("Unbinding...")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    protected void onDestroy() {
        if(mLoadingDialog != null) {
            if(mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvName) {
            startActivity(new Intent(this, ChangeLockNameActivity.class));
            return;
        }
        if(view.getId() == R.id.tvWifiName) {
            startActivity(new Intent(this, WifiSettingActivity.class));
            return;
        }
        if(view.getId() == R.id.clAutoLock) {
            startActivity(new Intent(this, AutoLockActivity.class));
            return;
        }
        if(view.getId() == R.id.clPrivateMode) {
            startActivity(new Intent(this, PrivateModeActivity.class));
            return;
        }
        if(view.getId() == R.id.clDuressCode) {
            startActivity(new Intent(this, DuressCodeActivity.class));
            return;
        }
        if(view.getId() == R.id.clDoorLockInformation) {
            startActivity(new Intent(this, DoorLockInformationActivity.class));
            return;
        }
        if(view.getId() == R.id.clGeoFenceLock) {
            startActivity(new Intent(this, GeoFenceUnlockActivity.class));
            return;
        }
        if(view.getId() == R.id.clDoorMagneticSwitch) {
            startActivity(new Intent(this, DoorMagnetAlignmentActivity.class));
            return;
        }
        if(view.getId() == R.id.clUnbind) {
            showUnbindDialog();
        }
    }

    @Override
    public Resources getResources() {
        // 更改布局适应
        return AdaptScreenUtils.adaptHeight(super.getResources(), 703);
    }

    private void initTestData() {
        mTvName.setText("Tester");
        mTvWifiName.setText("Kaadas123");
    }

    private void showUnbindDialog() {
        UnbindLockDialog dialog = new UnbindLockDialog(this);
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            unbindDevice();
        });
        dialog.show();
    }

    private void unbindDevice() {
        if(mLoadingDialog != null) {
            mLoadingDialog.show();
        }
        Observable<DeviceUnbindBeanRsp> observable = HttpRequest
                .getInstance().unbindDevice(App.getInstance().getUserBean().getToken(), mReq);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DeviceUnbindBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                
            }

            @Override
            public void onNext(@NonNull DeviceUnbindBeanRsp deviceUnbindBeanRsp) {
                if(mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                if(deviceUnbindBeanRsp.getCode() == null) {
                    return;
                }
                if(!deviceUnbindBeanRsp.getCode().equals("200")) {
                    return;
                }
                // TODO: 2021/2/6 抽离文字 和校对
                ToastUtils.showShort("Unbind success");
                finish();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

}
