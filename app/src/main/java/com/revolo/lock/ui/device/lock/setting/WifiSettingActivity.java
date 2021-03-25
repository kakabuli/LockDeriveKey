package com.revolo.lock.ui.device.lock.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockCloseWifiResponseBean;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.add.AddWifiActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;
import static com.revolo.lock.ble.BleCommandState.WIFI_CONTROL_OPEN;
import static com.revolo.lock.ble.BleProtocolState.CMD_DURESS_PWD_SWITCH;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : wifi设置
 */
public class WifiSettingActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private ConstraintLayout mClTip;
    private ImageView mIvWifiEnable;
    private boolean isWifiConnected = false;
    private TextView mTvSettingTitle, mTvWifiName, mTvSettingTip;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(!intent.hasExtra(Constant.BLE_DEVICE)) {
            // TODO: 2021/2/22 处理
            finish();
            return;
        }
        mBleDeviceLocal = intent.getParcelableExtra(Constant.BLE_DEVICE);
        if(mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_wifi_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_wifi_setting));
        mClTip = findViewById(R.id.clTip);
        mIvWifiEnable = findViewById(R.id.ivWifiEnable);
        mTvSettingTip = findViewById(R.id.tvSettingTip);
        mTvSettingTitle = findViewById(R.id.tvSettingTitle);
        mTvWifiName = findViewById(R.id.tvWifiName);
        initLoading("Setting...");
        updateUI();
        applyDebouncingClickListener(mIvWifiEnable, mTvSettingTitle, mTvWifiName);
    }

    @Override
    public void doBusiness() {
        if(mBleDeviceLocal.getConnectedType() != LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            initBleListener();
        }

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivWifiEnable) {
            if(isWifiConnected) {
                closeWifiFromMqtt();
            } else {
                String wifiName = mBleDeviceLocal.getConnectedWifiName();
                if(TextUtils.isEmpty(wifiName)) {
                    gotoAddWifiAct();
                } else {
                    openWifiFromBle();
                }
            }
            return;
        }
        if(view.getId() == R.id.tvWifiName || view.getId() == R.id.tvSettingTitle) {
            gotoAddWifiAct();
        }
    }

    private void gotoAddWifiAct() {
        Intent intent = new Intent(this, AddWifiActivity.class);
        intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
        startActivity(intent);
    }

    private void updateUI() {
        if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            // Wifi
            updateWifiState();
        } else if(mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            // 蓝牙
            updateBleState();
        } else {
            // TODO: 2021/2/26 do something
        }
    }

    private void closeWifiFromMqtt() {
        showLoading();
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.closeWifi(
                        mBleDeviceLocal.getEsn(),
                        BleCommandFactory
                                .getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS).safeSubscribe(new Observer<MqttData>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull MqttData mqttData) {
                if(TextUtils.isEmpty(mqttData.getFunc())) {
                    return;
                }
                if(mqttData.getFunc().equals(MqttConstant.CLOSE_WIFI)) {
                    dismissLoading();
                    Timber.d("closeWifiFromMqtt 关闭信息: %1s", mqttData);
                    WifiLockCloseWifiResponseBean bean;
                    try {
                        bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockCloseWifiResponseBean.class);
                    } catch (JsonSyntaxException e) {
                        Timber.e(e);
                        return;
                    }
                    if(bean == null) {
                        Timber.e("closeWifiFromMqtt bean == null");
                        return;
                    }
                    if(bean.getParams() == null) {
                        Timber.e("closeWifiFromMqtt bean.getParams() == null");
                        return;
                    }
                    if(bean.getCode() != 200) {
                        Timber.e("closeWifiFromMqtt code : %1d", bean.getCode());
                        if(bean.getCode() == 201) {
                            // TODO: 2021/3/4 抽离文字
                            ToastUtils.showShort("Close Wifi Fail!");
                        }
                        return;
                    }
                    refreshWifiConnectState();
                    updateUI();
                    // TODO: 2021/3/4 设置成功, 开启蓝牙连接
                    connectBle();
                }
                Timber.d("%1s", mqttData.toString());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void connectBle() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if(bleScanResult != null) {
                OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
                    @Override
                    public void onConnected(@NotNull String mac) {

                    }

                    @Override
                    public void onDisconnected(@NotNull String mac) {

                    }

                    @Override
                    public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                        if(value == null) {
                            return;
                        }
                        if(!mBleDeviceLocal.getMac().equals(mac)) {
                            return;
                        }
                        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                        if(bleBean == null) {
                            return;
                        }
                        if(bleBean.getOKBLEDeviceImp() == null) {
                            return;
                        }
                        if(bleBean.getPwd1() == null) {
                            return;
                        }
                        if(bleBean.getPwd2() == null) {
                            return;
                        }
                        BleResultProcess.setOnReceivedProcess(bleResultBean -> {
                            if(bleResultBean == null) {
                                Timber.e("%1s mOnReceivedProcess bleResultBean == null", mBleDeviceLocal.getMac());
                                return;
                            }
                            processBleResult(bleResultBean);
                        });
                        BleResultProcess.processReceivedData(
                                value,
                                bleBean.getPwd1(),
                                (bleBean.getPwd3() == null)?bleBean.getPwd2():bleBean.getPwd3(),
                                bleBean.getOKBLEDeviceImp().getBleScanResult());
                    }

                    @Override
                    public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

                    }

                    @Override
                    public void onAuthSuc(@NotNull String mac) {

                    }

                };
                bleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
                        onBleDeviceListener,false);
                bleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if(bleBean.getOKBLEDeviceImp() != null) {
                if(!bleBean.getOKBLEDeviceImp().isConnected()) {
                    bleBean.getOKBLEDeviceImp().connect(true);
                }
                bleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                bleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                bleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }

    }

    private void openWifiFromMqtt() {

    }

    private void openWifiFromBle() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("openWifiFromBle bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("openWifiFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd3 = bleBean.getPwd3();
        if(pwd1 == null) {
            Timber.e("openWifiFromBle pwd1 == null");
            return;
        }
        if(pwd3 == null) {
            Timber.e("openWifiFromBle pwd3 == null");
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(WIFI_CONTROL_OPEN, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void closeWifiFromBle() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean == null) {
            Timber.e("closeWifiFromBle bleBean == null");
            return;
        }
        if(bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("closeWifiFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        byte[] pwd1 = bleBean.getPwd1();
        byte[] pwd3 = bleBean.getPwd3();
        if(pwd1 == null) {
            Timber.e("closeWifiFromBle pwd1 == null");
            return;
        }
        if(pwd3 == null) {
            Timber.e("closeWifiFromBle pwd3 == null");
            return;
        }
        App.getInstance().writeControlMsg(BleCommandFactory.wifiSwitch(BleCommandState.WIFI_CONTROL_CLOSE, pwd1, pwd3), bleBean.getOKBLEDeviceImp());
    }

    private void updateWifiState() {
        runOnUiThread(() -> {
            mIvWifiEnable.setImageResource(R.drawable.ic_icon_switch_open);
            mClTip.setVisibility(View.VISIBLE);
            String wifiName = mBleDeviceLocal.getConnectedWifiName();
            mTvWifiName.setText(TextUtils.isEmpty(wifiName)?"":wifiName);
            isWifiConnected = true;
        });
    }

    private void updateBleState() {
        runOnUiThread(() -> {
            mIvWifiEnable.setImageResource(R.drawable.ic_icon_switch_close);
            mClTip.setVisibility(View.GONE);
            isWifiConnected = false;
        });
    }

    private void initBleListener() {
        BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(bleBean != null) {
            bleBean.setOnBleDeviceListener(new OnBleDeviceListener() {
                @Override
                public void onConnected(@NotNull String mac) {

                }

                @Override
                public void onDisconnected(@NotNull String mac) {

                }

                @Override
                public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                    if(value == null) {
                        Timber.e("initBleListener value == null");
                        return;
                    }
                    if(!mBleDeviceLocal.getMac().equals(mac)) {
                        Timber.e("initBleListener mac: %1s, local mac：%2s", mac, mBleDeviceLocal.getMac());
                        return;
                    }
                    BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                    if(bleBean == null) {
                        Timber.e("initBleListener bleBean == null");
                        return;
                    }
                    if(bleBean.getOKBLEDeviceImp() == null) {
                        Timber.e("initBleListener bleBean.getOKBLEDeviceImp() == null");
                        return;
                    }
                    if(bleBean.getPwd1() == null) {
                        Timber.e("initBleListener bleBean.getPwd1() == null");
                        return;
                    }
                    if(bleBean.getPwd3() == null) {
                        Timber.e("initBleListener bleBean.getPwd3() == null");
                        return;
                    }
                    BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                    BleResultProcess.processReceivedData(value,
                            bleBean.getPwd1(),
                            bleBean.getPwd3(),
                            bleBean.getOKBLEDeviceImp().getBleScanResult());
                }

                @Override
                public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

                }

                @Override
                public void onAuthSuc(@NotNull String mac) {

                }
            });
            // TODO: 2021/2/8 查询一下当前设置
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
        if(bean.getCMD() == CMD_WIFI_SWITCH) {
            processWifiSwitch(bean);
        }
    }

    private void processWifiSwitch(BleResultBean bean) {
        byte state = bean.getPayload()[0];
        if(state == 0x00) {
            refreshWifiConnectState();
            updateUI();
        } else {
            Timber.e("处理失败原因 state：%1s", ConvertUtils.int2HexString(BleByteUtil.byteToInt(state)));
        }
    }

    private void refreshWifiConnectState() {
        isWifiConnected = !isWifiConnected;
        mBleDeviceLocal.setConnectedType(isWifiConnected? LocalState.DEVICE_CONNECT_TYPE_WIFI:LocalState.DEVICE_CONNECT_TYPE_BLE);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

}
