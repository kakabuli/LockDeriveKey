package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.CheckAllOTABeanReq;
import com.revolo.lock.bean.request.CheckOTABeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.StartAllOTAUpdateBeanReq;
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
import com.revolo.lock.bean.respone.CheckAllOTABeanRsp;
import com.revolo.lock.bean.respone.CheckOTABeanRsp;
import com.revolo.lock.bean.respone.StartAllOTAUpdateBeanRsp;
import com.revolo.lock.bean.respone.StartOTAUpdateBeanRsp;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.OTAUpdateDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.bean.eventbean.WifiLockOperationEventBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.HARD_TYPE_FRONT_PANEL;
import static com.revolo.lock.ble.BleCommandState.HARD_TYPE_WIFI_LOCK;
import static com.revolo.lock.ble.BleProtocolState.CMD_CHECK_HARD_VER;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_MQTT;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 门锁信息
 */
public class DoorLockInformationActivity extends BaseActivity {

    private TextView mTvWifiVersion;
    private TextView mTvFirmwareVersion;
    private TextView mTvSalesmodel;
    private TextView mTvBluetoothMac;
    private TextView mTvWifi;
    private View mVVersion, vFirmwareVersion;
    private DeviceUnbindBeanReq mReq;
    private BleDeviceLocal mBleDeviceLocal;

    private CheckOTABeanRsp mCheckFirmwareOTABeanRsp, mCheckWifiOTABeanRsp;

    private CheckAllOTABeanRsp.DataBean mAllOTADataBean;

    private boolean isCanUpdateFirmwareVer = false;
    private boolean isCanUpdateWifiVer = false;

    private OTAUpdateDialog mOTAUpdateDialog;
    private MessageDialog mMessageDialog;

    // -1 不做任何升级， 2 wifi锁， 6 前板
    private int mUpdateType = -1;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.UNBIND_REQ)) {
            mReq = intent.getParcelableExtra(Constant.UNBIND_REQ);
        } else {
            // TODO: 2021/2/6 提示没从上一个页面传递数据过来
            finish();
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
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
        mTvSalesmodel=findViewById(R.id.door_lock_info_sales_model);
        mTvBluetoothMac=findViewById(R.id.door_lock_info_bluetooth_mac);
        mTvWifi=findViewById(R.id.door_lock_info_wifi);
        applyDebouncingClickListener(mTvFirmwareVersion, mTvWifiVersion);

        String esn = mReq.getWifiSN();
        tvLockSn.setText(TextUtils.isEmpty(esn) ? "" : esn);
        initLoading(getString(R.string.t_load_content_loading));

        onRegisterEventBus();

        mOTAUpdateDialog = new OTAUpdateDialog.Builder(this).setMessage(getString(R.string.dialog_content_ota_update)).create();

        mMessageDialog = new MessageDialog(this);
        mMessageDialog.setOnListener(v -> {
            if (mMessageDialog != null) {
                mMessageDialog.dismiss();
            }
        });

        mTvSalesmodel.setText("WFP");
        mTvBluetoothMac.setText(mBleDeviceLocal.getMac());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常

            }


        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
                processBleResult(lockMessage.getBleResultBea());
            }  //数据异常


        } else if (lockMessage.getMessgaeType() == MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                //数据正常
            } else {
                //数据异常

            }
        }
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
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            initBleListener();
        } else {
            String fireVer = mBleDeviceLocal.getLockVer();
            String wifiVer = mBleDeviceLocal.getWifiVer();
            if (!TextUtils.isEmpty(fireVer) || !TextUtils.isEmpty(wifiVer)) {
                checkAllOTAVer(fireVer, wifiVer);
            }
        }
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.tvFirmwareVersion) {
            if (isCanUpdateFirmwareVer) {
                showAllUpdateVerDialog();
                mUpdateType = 6;
            }
            return;
        }
        if (view.getId() == R.id.tvWifiVersion) {
            if (isCanUpdateWifiVer) {
                showAllUpdateVerDialog();
                mUpdateType = 2;
            }
        }
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            String lockVer = mBleDeviceLocal.getLockVer();
            mTvFirmwareVersion.setText(TextUtils.isEmpty(lockVer) ? "" : lockVer.trim());
            String wifiVer = mBleDeviceLocal.getWifiVer();
            mTvWifiVersion.setText(TextUtils.isEmpty(wifiVer) ? "" : wifiVer.trim());
        });
    }


    private void processBleResult(BleResultBean bean) {
        if (bean.getCMD() == CMD_CHECK_HARD_VER) {
            if (bean.getPayload()[0] == 0x00) {
                if (bean.getPayload()[1] == HARD_TYPE_FRONT_PANEL) {
                    refreshLockVerFromBle(bean);
                } else if (bean.getPayload()[1] == HARD_TYPE_WIFI_LOCK) {
                    refreshWifiVerFromBle(bean);
                } else {
                    // TODO: 2021/2/7 其他的数据处理
                }
            } else {
                // TODO: 2021/2/7 信息失败了的操作
            }
        }
    }

    private void refreshLockVerFromBle(BleResultBean bean) {
        // 锁的前板固件版本
        runOnUiThread(() -> {
            mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
            mTvFirmwareVersion.setText(mBleDeviceLocal.getLockVer());
        });
    }

    private void refreshWifiVerFromBle(BleResultBean bean) {
        // 锁的wifi版本
        runOnUiThread(() -> {
            mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
            mTvWifiVersion.setText(mBleDeviceLocal.getWifiVer());
        });
    }

    private void initBleListener() {
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("initBleListener bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("initBleListener bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("initBleListener bleBean.getPwd3() == null");
            return;
        }
        // 查询前板的版本信息
        LockMessage message = new LockMessage();
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setBytes(BleCommandFactory
                .checkHardVer(HARD_TYPE_FRONT_PANEL,
                        bleBean.getPwd1(),
                        bleBean.getPwd3()));
        message.setMessageType(3);
        EventBus.getDefault().post(message);
        // 查询wifi的版本信息
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    LockMessage message2 = new LockMessage();
                    message2.setMessageType(3);
                    message2.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
                    message2.setBytes(BleCommandFactory
                            .checkHardVer(HARD_TYPE_WIFI_LOCK,
                                    bleBean.getPwd1(),
                                    bleBean.getPwd3()));
                    EventBus.getDefault().post(message2);
                },
                100);
    }

    /*------------------------- 前板 ------------------------------*/

    private void checkFirmwareOTAVer(String ver) {
        if (!checkNetConnectFail()) {
            return;
        }
        CheckOTABeanReq req = new CheckOTABeanReq();
        // TODO: 2021/2/9 先暂时使用16，后面制定好规范
        req.setCustomer(16);
        req.setDeviceName(mBleDeviceLocal.getEsn());
        // 暂时使用 2为WIFI锁，6为前面板（1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板）
        req.setDevNum(1);
        req.setVersion(ver);
        showLoading();
        Observable<CheckOTABeanRsp> observable = HttpRequest.getInstance()
                .checkOtaVer(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<CheckOTABeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull CheckOTABeanRsp checkOTABeanRsp) {
                // TODO: 2021/2/9 所有的都要判断处理
                dismissLoading();
                String code = checkOTABeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("checkOTABeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    Timber.e("checkOTAVer code: %1s  msg: %2s",
                            checkOTABeanRsp.getCode(), checkOTABeanRsp.getMsg());
                    return;
                }
                if (checkOTABeanRsp.getData() == null) {
                    Timber.e("mCheckOTABeanRsp.getData() == null");
                    return;
                }
                mCheckFirmwareOTABeanRsp = checkOTABeanRsp;
                isCanUpdateFirmwareVer = !mCheckFirmwareOTABeanRsp.getData().getFileVersion().equalsIgnoreCase(ver);
                vFirmwareVersion.setVisibility(isCanUpdateFirmwareVer ? View.VISIBLE : View.GONE);
                checkWifiOTAVer();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // TODO: 2021/2/9 请求失败的处理方式
                dismissLoading();
                Timber.e(e);
                checkWifiOTAVer();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showFirmwareUpdateVerDialog() {
        SelectDialog selectDialog = new SelectDialog(this);
        selectDialog.setMessage(getString(R.string.dialog_tip_there_is_a_new_version_available_do_you_want_to_update));
        selectDialog.setOnConfirmListener(v -> {
            selectDialog.dismiss();
            checkOrUseFirmwareOTAUpdateVer();
        });
        selectDialog.setOnCancelClickListener(v -> selectDialog.dismiss());
        selectDialog.show();
    }

    private void checkOrUseFirmwareOTAUpdateVer() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (mCheckFirmwareOTABeanRsp == null) {
            return;
        }
        if (mCheckFirmwareOTABeanRsp.getData() == null) {
            return;
        }
        StartOTAUpdateBeanReq req = new StartOTAUpdateBeanReq();
        req.setDevNum(mCheckFirmwareOTABeanRsp.getData().getDevNum());
        req.setFileLen(mCheckFirmwareOTABeanRsp.getData().getFileLen());
        req.setFileMd5(mCheckFirmwareOTABeanRsp.getData().getFileMd5());
        req.setFileUrl(mCheckFirmwareOTABeanRsp.getData().getFileUrl());
        req.setFileVersion(mCheckFirmwareOTABeanRsp.getData().getFileVersion());
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
                String code = startOTAUpdateBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("startOTAUpdateBeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    String msg = startOTAUpdateBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("checkOrUseOTAUpdateVer code: %1s,  msg: %2s", code, msg);
                    return;
                }
                if (mOTAUpdateDialog != null) {
                    mOTAUpdateDialog.setTimeOut(3);
                    mOTAUpdateDialog.show();
                }
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


    /*---------------------------- wifi锁 -----------------------------*/

    private void checkWifiOTAVer() {
        String wifiVer = mBleDeviceLocal.getWifiVer();
        if (TextUtils.isEmpty(wifiVer)) {
            return;
        }
        checkWifiOTAVer(wifiVer);
    }

    private void checkWifiOTAVer(String ver) {
        if (!checkNetConnectFail()) {
            return;
        }
        CheckOTABeanReq req = new CheckOTABeanReq();
        // TODO: 2021/2/9 先暂时使用16，后面制定好规范
        req.setCustomer(16);
        req.setDeviceName(mBleDeviceLocal.getEsn());
        // 暂时使用 2为WIFI锁，6为前面板（1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板）
        req.setDevNum(2);
        req.setVersion(ver);
        showLoading();
        Observable<CheckOTABeanRsp> observable = HttpRequest.getInstance()
                .checkOtaVer(App.getInstance().getUserBean().getToken(), req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<CheckOTABeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull CheckOTABeanRsp checkOTABeanRsp) {
                // TODO: 2021/2/9 所有的都要判断处理
                dismissLoading();
                String code = checkOTABeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("checkOTABeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    String msg = checkOTABeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("checkOTAVer code: %1s  msg: %2s", code, msg);
                    return;
                }
                if (checkOTABeanRsp.getData() == null) {
                    Timber.e("mCheckOTABeanRsp.getData() == null");
                    return;
                }
                mCheckWifiOTABeanRsp = checkOTABeanRsp;
                isCanUpdateWifiVer = !mCheckWifiOTABeanRsp.getData().getFileVersion().equalsIgnoreCase(ver);
                mVVersion.setVisibility(isCanUpdateWifiVer ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // TODO: 2021/2/9 请求失败的处理方式
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showWifiUpdateVerDialog() {
        SelectDialog selectDialog = new SelectDialog(this);
        selectDialog.setMessage(getString(R.string.dialog_tip_there_is_a_new_version_available_do_you_want_to_update));
        selectDialog.setOnConfirmListener(v -> {
            selectDialog.dismiss();
            checkOrUseWifiOTAUpdateVer();
        });
        selectDialog.setOnCancelClickListener(v -> selectDialog.dismiss());
        selectDialog.show();
    }

    private void checkOrUseWifiOTAUpdateVer() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (mCheckWifiOTABeanRsp == null) {
            return;
        }
        if (mCheckWifiOTABeanRsp.getData() == null) {
            return;
        }
        StartOTAUpdateBeanReq req = new StartOTAUpdateBeanReq();
        req.setDevNum(mCheckWifiOTABeanRsp.getData().getDevNum());
        req.setFileLen(mCheckWifiOTABeanRsp.getData().getFileLen());
        req.setFileMd5(mCheckWifiOTABeanRsp.getData().getFileMd5());
        req.setFileUrl(mCheckWifiOTABeanRsp.getData().getFileUrl());
        req.setFileVersion(mCheckWifiOTABeanRsp.getData().getFileVersion());
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
                String code = startOTAUpdateBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e(" checkOrUseWifiOTAUpdateVer startOTAUpdateBeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    String msg = startOTAUpdateBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("checkOrUseWifiOTAUpdateVer code: %1s,  msg: %2s", code, msg);
                    return;
                }
//                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_ota_updating);
                if (mOTAUpdateDialog != null) {
                    mOTAUpdateDialog.setTimeOut(3);
                    mOTAUpdateDialog.show();
                }
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

    /*----------------------------- 多固件版本升级 ------------------------------*/

    private void checkAllOTAVer(String firmwareVer, String wifiVer) {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("checkAllOTAVer App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("checkAllOTAVer token is empty");
            return;
        }
        List<CheckAllOTABeanReq.VersionsBean> versionsBeans = new ArrayList<>();
        if (!TextUtils.isEmpty(firmwareVer)) {
            CheckAllOTABeanReq.VersionsBean versionsBean = new CheckAllOTABeanReq.VersionsBean();
            // TODO: 2021/3/17 后续需要抽离enum
            // 暂时使用 2为WIFI锁，6为前面板（1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板）
            versionsBean.setDevNum(6);
            versionsBean.setVersion(firmwareVer);
            versionsBeans.add(versionsBean);
        }
        if (!TextUtils.isEmpty(wifiVer)) {
            CheckAllOTABeanReq.VersionsBean versionsBean = new CheckAllOTABeanReq.VersionsBean();
            // 暂时使用 2为WIFI锁，6为前面板（1为WIFI模块，2为WIFI锁，3为人脸模组，4为视频模组，5为视频模组微控制器，6为前面板，7为后面板）
            versionsBean.setDevNum(2);
            versionsBean.setVersion(wifiVer);
            versionsBeans.add(versionsBean);
        }
        CheckAllOTABeanReq req = new CheckAllOTABeanReq();
        req.setCustomer(16);
        req.setDeviceName(mBleDeviceLocal.getEsn());
        req.setVersions(versionsBeans);
        showLoading();
        Observable<CheckAllOTABeanRsp> observable = HttpRequest.getInstance().checkAllOtaVer(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<CheckAllOTABeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull CheckAllOTABeanRsp checkAllOTABeanRsp) {
                dismissLoading();
                String code = checkAllOTABeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("checkAllOTAVer checkAllOTABeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    String msg = checkAllOTABeanRsp.getMsg();
                    Timber.e("checkAllOTAVer code: %1s  msg: %2s",
                            checkAllOTABeanRsp.getCode(), msg);
//                    if (!TextUtils.isEmpty(msg)) {
//                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
//                    }
                    return;
                }
                if (checkAllOTABeanRsp.getData() == null) {
                    Timber.e("checkAllOTAVer checkAllOTABeanRsp.getData() == null");
                    return;
                }
                // TODO: 2021/3/17
                mAllOTADataBean = checkAllOTABeanRsp.getData();
                if (mAllOTADataBean.getUpgradeTask() == null) {
                    Timber.e("checkAllOTAVer mAllOTADataBean.getUpgradeTask() == null");
                    return;
                }
                if (mAllOTADataBean.getUpgradeTask().isEmpty()) {
                    Timber.e("checkAllOTAVer mAllOTADataBean.getUpgradeTask().isEmpty()");
                    return;
                }
                for (CheckAllOTABeanRsp.DataBean.UpgradeTaskBean taskBean : mAllOTADataBean.getUpgradeTask()) {
                    // TODO: 2021/3/17 数字抽离
                    if (taskBean.getDevNum() == 6) {
                        isCanUpdateFirmwareVer = !taskBean.getFileVersion().equalsIgnoreCase(firmwareVer);
                        vFirmwareVersion.setVisibility(isCanUpdateFirmwareVer ? View.VISIBLE : View.GONE);
                    } else if (taskBean.getDevNum() == 2) {
                        isCanUpdateWifiVer = !taskBean.getFileVersion().equalsIgnoreCase(wifiVer);
                        mVVersion.setVisibility(isCanUpdateWifiVer ? View.VISIBLE : View.GONE);
                    }
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

    private void showAllUpdateVerDialog() {
        SelectDialog selectDialog = new SelectDialog(this);
        selectDialog.setMessage(getString(R.string.dialog_tip_there_is_a_new_version_available_do_you_want_to_update));
        selectDialog.setOnConfirmListener(v -> {
            selectDialog.dismiss();
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
                checkOrUseAllOTAUpdateVer();
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.tip_content_ble_not_update));
            }
        });
        selectDialog.setOnCancelClickListener(v -> selectDialog.dismiss());
        selectDialog.show();
    }

    private void checkOrUseAllOTAUpdateVer() {
        if (!checkNetConnectFail()) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (mAllOTADataBean == null) {
            return;
        }
        if (mAllOTADataBean.getUpgradeTask() == null) {
            return;
        }
        if (mAllOTADataBean.getUpgradeTask().isEmpty()) {
            return;
        }

        StartAllOTAUpdateBeanReq req = new StartAllOTAUpdateBeanReq();
        List<StartAllOTAUpdateBeanReq.UpgradeTaskBean> taskBeans = new ArrayList<>();
        for (CheckAllOTABeanRsp.DataBean.UpgradeTaskBean taskBean : mAllOTADataBean.getUpgradeTask()) {
            StartAllOTAUpdateBeanReq.UpgradeTaskBean bean = new StartAllOTAUpdateBeanReq.UpgradeTaskBean();
            bean.setDevNum(taskBean.getDevNum());
            bean.setFileLen(taskBean.getFileLen());
            bean.setFileMd5(taskBean.getFileMd5());
            bean.setFileUrl(taskBean.getFileUrl());
            bean.setFileVersion(taskBean.getFileVersion());
            if (mUpdateType == taskBean.getDevNum()) {
                taskBeans.add(bean);
            }
        }
        req.setUpgradeTask(taskBeans);
        req.setWifiSN(mBleDeviceLocal.getEsn());

        showLoading();
        Observable<StartAllOTAUpdateBeanRsp> observable = HttpRequest
                .getInstance()
                .startAllOtaUpdate(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<StartAllOTAUpdateBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull StartAllOTAUpdateBeanRsp beanRsp) {
                dismissLoading();
                String code = beanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e(" checkOrUseAllOTAUpdateVer startOTAUpdateBeanRsp.getCode() is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, DoorLockInformationActivity.this);
                        return;
                    }
                    String msg = beanRsp.getMsg();
                    Timber.e("checkOrUseAllOTAUpdateVer code: %1s,  msg: %2s",
                            beanRsp.getCode(), msg);
                    if (TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
//                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_ota_update_success);
                if (mOTAUpdateDialog != null) {
                    mOTAUpdateDialog.setTimeOut(mUpdateType == 6 ? 1 : 3);
                    mOTAUpdateDialog.show();
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getOTAUpdateResult(LockMessageRes lockMessageRes) {
        if (lockMessageRes != null) {
            WifiLockOperationEventBean bean = (WifiLockOperationEventBean) lockMessageRes.getWifiLockBaseResponseBean();
            if (bean != null && bean.getEventtype().equals("otaResult")) {
                if (mOTAUpdateDialog != null) {
                    mOTAUpdateDialog.dismiss();
                }
                WifiLockOperationEventBean.EventparamsBean eventparams = bean.getEventparams();
                if (eventparams != null) {
                    int returnCode = eventparams.getReturnCode();
                    if (returnCode == 200) { // 成功
                        int status = eventparams.getStatus();
                        if (status == 3) { // 升级完成
                            if (mMessageDialog != null) {
                                mMessageDialog.setMessage(getString(R.string.tip_content_ota_update_success));
                                mMessageDialog.show();
                            }
                            int devNum = eventparams.getDevNum();
                            if (devNum == 2) { // wifi锁
                                String wifiVer = eventparams.getSW();
                                mTvWifiVersion.setText(wifiVer);
                                mBleDeviceLocal.setWifiVer(wifiVer);
                                isCanUpdateWifiVer = false;
                                mVVersion.setVisibility(isCanUpdateWifiVer ? View.VISIBLE : View.GONE);
                            } else if (devNum == 6) { //
                                String fireVer = eventparams.getSW();
                                mTvFirmwareVersion.setText(fireVer);
                                mBleDeviceLocal.setLockVer(fireVer);
                                isCanUpdateFirmwareVer = false;
                                vFirmwareVersion.setVisibility(isCanUpdateFirmwareVer ? View.VISIBLE : View.GONE);
                            }
                            App.getInstance().setBleDeviceLocal(mBleDeviceLocal);
                        }
                    } else {
                        int hwerrcode = eventparams.getHwerrcode();
                        Timber.d(hwerrcode == 1 ? "下载文件失败" : hwerrcode == 2 ? "文件MD5校验失败" : hwerrcode == 3 ? "版本号相同无法升级" : "其他错误");
                        if (mMessageDialog != null) {
                            mMessageDialog.setMessage(getString(R.string.tip_content_ota_update_failed));
                            mMessageDialog.show();
                        }
                    }
                }
            }
        }
    }
}
