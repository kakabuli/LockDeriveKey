package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.UnbindLockDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.widget.iosloading.CustomerLoadingDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;

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
    private ImageView ivMuteEnable;


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
        ivMuteEnable = findViewById(R.id.ivMuteEnable);
        applyDebouncingClickListener(mTvName, mTvWifiName,
                findViewById(R.id.clAutoLock), findViewById(R.id.clPrivateMode),
                findViewById(R.id.clDuressCode), findViewById(R.id.clDoorLockInformation),
                findViewById(R.id.clGeoFenceLock), findViewById(R.id.clDoorMagneticSwitch),
                findViewById(R.id.clUnbind), findViewById(R.id.clMute));
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
        initBleListener();
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
            Intent intent = new Intent(this, DoorLockInformationActivity.class);
            intent.putExtra(Constant.UNBIND_REQ, mReq);
            startActivity(intent);
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
            return;
        }
        if(view.getId() == R.id.clMute) {
            mute();
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

    private boolean isMute = false;

    private void mute() {
        // 0x00：Silent Mode静音
        // 0x01：Low Volume低音量
        // 0x02：High Volume高音量
        // TODO: 2021/2/8 后面需要动态修改
        byte[] value = new byte[1];
        value[0] = (byte) (isMute?0x01:0x00);
        App.getInstance().writeControlMsg(BleCommandFactory.lockParameterModificationCommand((byte) 0x02,
                (byte) 0x01, value, App.getInstance().getBleBean().getPwd1(), App.getInstance().getBleBean().getPwd3()));
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

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        // TODO: 2021/2/7 需要初始化设置和设置各种参数的回调
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHANGED) {
            processMute(bean);
        } else if(bean.getCMD() == BleProtocolState.CMD_LOCK_UPLOAD) {
            lockUpdateInfo(bean);
        }
    }

    private void processMute(BleResultBean bean) {
        if(bean.getPayload()[0] == 0x00) {
            // TODO: 2021/2/8 处理数据
            runOnUiThread(() -> {
                isMute = !isMute;
                ivMuteEnable.setImageResource(isMute?R.drawable.ic_icon_switch_open:R.drawable.ic_icon_switch_close);
            });
        } else {
            // TODO: 2021/2/7 信息失败了的操作
            ToastUtils.showShort("Setting Mute Fail");
        }
    }

    private void lockUpdateInfo(BleResultBean bean) {
        // TODO: 2021/2/7 锁操作上报
        int eventType = bean.getPayload()[0];
        int eventSource = bean.getPayload()[1];
        int eventCode = bean.getPayload()[2];
        int userID = bean.getPayload()[3];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 4, time, 0, time.length);
        // TODO: 2021/2/8 要做时间都是ffffffff的处理判断
        long realTime = (BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) + Constant.WILL_ADD_TIME)*1000;
        Timber.d("CMD: %1d, eventType: %2d, eventSource: %3d, eventCode: %4d, userID: %5d, time: %6d",
                bean.getCMD(), eventType, eventSource, eventCode, userID, realTime);
    }

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onReceivedValue(String uuid, byte[] value) {
                if(value == null) {
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }
        });
        // TODO: 2021/2/8 查询一下当前设置
    }

}
