package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.CheckOTABeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
import com.revolo.lock.bean.respone.CheckOTABeanRsp;
import com.revolo.lock.bean.respone.StartOTAUpdateBeanRsp;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import java.nio.charset.StandardCharsets;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHECK;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门锁信息
 */
public class DoorLockInformationActivity extends BaseActivity {

    private TextView mTvWifiVersion;
    private TextView mTvFirmwareVersion;
    private View mVVersion, vFirmwareVersion;
    private DeviceUnbindBeanReq mReq;

    private CheckOTABeanRsp mCheckOTABeanRsp;

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
        return R.layout.activity_door_lock_infomation;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_door_lock_information));
        mVVersion = findViewById(R.id.vVersion);
        vFirmwareVersion = findViewById(R.id.vFirmwareVersion);
        TextView tvLockSn = findViewById(R.id.tvLockSn);
        mTvWifiVersion = findViewById(R.id.tvWifiVersion);
        mTvFirmwareVersion = findViewById(R.id.tvFirmwareVersion);
        applyDebouncingClickListener(mTvFirmwareVersion, mTvWifiVersion);

        String esn = mReq.getWifiSN();
        tvLockSn.setText(TextUtils.isEmpty(esn)?"":esn);

    }

    @Override
    public void doBusiness() {
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvFirmwareVersion) {
            showUpdateVerDialog();
            return;
        }
        if(view.getId() == R.id.tvWifiVersion) {
            // TODO: 2021/2/7 检查固件版本，并可能升级
        }
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        // TODO: 2021/2/7 获取版本信息
        if(bean.getCMD() == CMD_LOCK_PARAMETER_CHECK) {
            if(bean.getPayload()[0] == 0x00) {
                if(bean.getPayload()[1] == 0x03) {
                    // 锁的软件版本
                    runOnUiThread(() -> {
                        byte[] verBytes = new byte[9];
                        System.arraycopy(bean.getPayload(), 2, verBytes, 0, verBytes.length);
                        String verStr = new String(verBytes, StandardCharsets.UTF_8);
                        mTvFirmwareVersion.setText(verStr);
                        checkOTAVer(verStr);
                    });

                } else {
                    // TODO: 2021/2/7 其他的数据处理
                }
            } else {
                // TODO: 2021/2/7 信息失败了的操作
            }
        }
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

            @Override
            public void onAuthSuc() {

            }
        });
        new Handler(Looper.getMainLooper()).postDelayed(() -> App.getInstance().writeControlMsg(BleCommandFactory
                .lockParameterCheckCommand((byte) 0x03,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3())), 50);
    }

    private void checkOTAVer(String ver) {
        CheckOTABeanReq req = new CheckOTABeanReq();
        // TODO: 2021/2/9 先暂时使用16，后面制定好规范
        req.setCustomer(16);
        req.setDeviceName(App.getInstance().getBleBean().getEsn());
        // 1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器。
        req.setDevNum(2);
        req.setVersion(ver);
        Observable<CheckOTABeanRsp> observable = HttpRequest.getInstance()
                .checkOtaVer(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<CheckOTABeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull CheckOTABeanRsp checkOTABeanRsp) {
                // TODO: 2021/2/9 所有的都要判断处理
                if(TextUtils.isEmpty(checkOTABeanRsp.getCode())) {
                    Timber.e("checkOTABeanRsp.getCode() is empty");
                    return;
                }
                if(!checkOTABeanRsp.getCode().equals("200")) {
                    Timber.e("checkOTAVer code: %1s  msg: %2s",
                            checkOTABeanRsp.getCode(), checkOTABeanRsp.getMsg());
                    return;
                }
                if(mCheckOTABeanRsp.getData() == null) {
                    Timber.e("mCheckOTABeanRsp.getData() == null");
                    return;
                }
                mCheckOTABeanRsp = checkOTABeanRsp;
                vFirmwareVersion.setVisibility(mCheckOTABeanRsp.getData().getFileVersion().equalsIgnoreCase(ver)?View.GONE:View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // TODO: 2021/2/9 请求失败的处理方式
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showUpdateVerDialog() {
        SelectDialog selectDialog = new SelectDialog(this);
        selectDialog.setMessage(getString(R.string.dialog_tip_there_is_a_new_version_available_do_you_want_to_update));
        selectDialog.setOnConfirmListener(v -> {
            selectDialog.dismiss();
            checkOrUseOTAUpdateVer();
        });
        selectDialog.setOnCancelClickListener(v -> selectDialog.dismiss());
        selectDialog.show();
    }

    private void checkOrUseOTAUpdateVer() {
        if(mCheckOTABeanRsp == null) {
            return;
        }
        if(mCheckOTABeanRsp.getData() == null) {
            return;
        }
        StartOTAUpdateBeanReq req = new StartOTAUpdateBeanReq();
        req.setDevNum(mCheckOTABeanRsp.getData().getDevNum());
        req.setFileLen(mCheckOTABeanRsp.getData().getFileLen());
        req.setFileMd5(mCheckOTABeanRsp.getData().getFileMd5());
        req.setFileUrl(mCheckOTABeanRsp.getData().getFileUrl());
        req.setFileVersion(mCheckOTABeanRsp.getData().getFileVersion());
        req.setWifiSN(mReq.getWifiSN());
        Observable<StartOTAUpdateBeanRsp> observable = HttpRequest
                .getInstance()
                .startOtaUpdate(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<StartOTAUpdateBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                
            }

            @Override
            public void onNext(@NonNull StartOTAUpdateBeanRsp startOTAUpdateBeanRsp) {
                if(TextUtils.isEmpty(startOTAUpdateBeanRsp.getCode())) {
                    Timber.e("startOTAUpdateBeanRsp.getCode() is empty");
                    return;
                }
                if(!startOTAUpdateBeanRsp.getCode().equals("200")) {
                    Timber.e("checkOrUseOTAUpdateVer code: %1s,  msg: %2s",
                            startOTAUpdateBeanRsp.getCode(), startOTAUpdateBeanRsp.getMsg());
                    return;
                }
                // TODO: 2021/2/9 完成OTA升级推送 后面提示语需要修改
                ToastUtils.showShort("OTA update success");
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
