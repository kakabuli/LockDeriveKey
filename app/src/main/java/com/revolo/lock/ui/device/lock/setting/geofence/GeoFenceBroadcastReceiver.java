package com.revolo.lock.ui.device.lock.setting.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.MqttConstant;
import com.revolo.lock.mqtt.bean.MqttData;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockApproachOpenResponseBean;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.DEFAULT_TIMEOUT_SEC_VALUE;

/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GeoFenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        Timber.d("onReceive:work");

        //Toast.makeText(context, "Geofence triggered.....", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Timber.d("onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Timber.d("onReceive: %1s", geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                ToastUtils.showShort("You have entered the range of the geo fence");
                notificationHelper.sendHighPriorityNotification("You have entered the range of the geo fence", "", MapActivity.class);
                BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
                if(deviceLocal == null) {
                    return;
                }
                publishApproachOpen(deviceLocal.getEsn(), deviceLocal.getSetElectricFenceTime());
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                ToastUtils.showShort("GEO_FENCE_TRANSITION_DWELL");
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MapActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                ToastUtils.showShort("You have exited the geo fence");
                notificationHelper.sendHighPriorityNotification("You have exited the geo fence", "", MapActivity.class);
                break;
        }
    }

    private void publishApproachOpen(String wifiID, int broadcastTime) {
        BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
        if(deviceLocal == null) {
            return;
        }
        App.getInstance().setUsingGeoFenceBleDeviceLocal(deviceLocal);
        App.getInstance().getMqttService().mqttPublish(MqttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                MqttCommandFactory.approachOpen(wifiID, broadcastTime,
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .safeSubscribe(new Observer<MqttData>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull MqttData mqttData) {
                        if(TextUtils.isEmpty(mqttData.getFunc())) {
                            Timber.e("publishApproachOpen mqttData.getFunc() is empty");
                            return;
                        }
                        if(mqttData.getFunc().equals(MqttConstant.APP_ROACH_OPEN)) {
                            Timber.d("publishApproachOpen 无感开门: %1s", mqttData);
                            WifiLockApproachOpenResponseBean bean;
                            try {
                                bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockApproachOpenResponseBean.class);
                            } catch (JsonSyntaxException e) {
                                Timber.e(e);
                                return;
                            }
                            if(bean == null) {
                                Timber.e("publishApproachOpen bean == null");
                                return;
                            }
                            if(bean.getParams() == null) {
                                Timber.e("publishApproachOpen bean.getParams() == null");
                                return;
                            }
                            if(bean.getCode() != 200) {
                                Timber.e("publishApproachOpen code : %1d", bean.getCode());
                                return;
                            }
                            // TODO: 2021/3/5 开启成功，然后开启蓝牙并不断搜索设备
                            connectBle();
                        }
                        Timber.d("publishApproachOpen %1s", mqttData.toString());
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        // TODO: 2021/3/3 错误处理
                        // 超时或者其他错误
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    // TODO: 2021/4/7 地理围栏功能后期需要优化,需要放到全局
    private boolean isRestartConnectingBle = false;
    private BleBean mBleBean;
    private BleDeviceLocal mBleDeviceLocal;

    private void connectBle() {
        mBleDeviceLocal = App.getInstance().getUsingGeoFenceBleDeviceLocal();
        if(mBleDeviceLocal == null) {
            return;
        }
        isRestartConnectingBle = true;
        final OnBleDeviceListener onBleDeviceListener = new OnBleDeviceListener() {
            @Override
            public void onConnected(@NotNull String mac) {

            }

            @Override
            public void onDisconnected(@NotNull String mac) {

            }

            @Override
            public void onReceivedValue(@NotNull String mac, String uuid, byte[] value) {
                if(value == null) {
                    Timber.e("mOnBleDeviceListener value == null");
                    return;
                }
                if(!mBleDeviceLocal.getMac().equals(mac)) {
                    Timber.e("mOnBleDeviceListener mac: %1s, localMac: %2s", mac, mBleDeviceLocal.getMac());
                    return;
                }
                BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                if(bleBean == null) {
                    Timber.e("mOnBleDeviceListener bleBean == null");
                    return;
                }
                if(bleBean.getOKBLEDeviceImp() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getOKBLEDeviceImp() == null");
                    return;
                }
                if(bleBean.getPwd1() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getPwd1() == null");
                    return;
                }
                if(bleBean.getPwd3() == null) {
                    Timber.e("mOnBleDeviceListener bleBean.getPwd3() == null");
                    return;
                }
                BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
                BleResultProcess.processReceivedData(value, bleBean.getPwd1(), bleBean.getPwd3(),
                        bleBean.getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success) {

            }

            @Override
            public void onAuthSuc(@NotNull String mac) {
                // 配对成功
                if(mac.equals(mBleDeviceLocal.getMac())) {
                    isRestartConnectingBle = false;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BleBean bleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
                            if(bleBean == null) {
                                Timber.e("mOnBleDeviceListener bleBean == null");
                                return;
                            }
                            // TODO: 2021/4/7 抽离0x01
                            App.getInstance().writeControlMsg(BleCommandFactory
                                    .setKnockDoorAndUnlockTime(0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
                        }
                    }, 200);
                }
            }

        };
        mBleBean = App.getInstance().getBleBeanFromMac(mBleDeviceLocal.getMac());
        if(mBleBean == null) {
            BLEScanResult bleScanResult = ConvertUtils.bytes2Parcelable(mBleDeviceLocal.getScanResultJson(), BLEScanResult.CREATOR);
            if(bleScanResult != null) {
                mBleBean = App.getInstance().connectDevice(
                        bleScanResult,
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()),
                        onBleDeviceListener,false);
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 处理为空的情况
            }
        } else {
            if(mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.setOnBleDeviceListener(onBleDeviceListener);
                if(!mBleBean.getOKBLEDeviceImp().isConnected()) {
                    mBleBean.getOKBLEDeviceImp().connect(true);
                }
                mBleBean.setPwd1(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()));
                mBleBean.setPwd2(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()));
                mBleBean.setEsn(mBleDeviceLocal.getEsn());
            } else {
                // TODO: 2021/1/26 为空的处理
            }
        }
        // 1分钟后判断设备是否连接成功，否就恢复wifi状态，每秒判断一次是否配对设备成功
        mCountDownTimer.start();
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        processBleResult(bleResultBean);
    };

    private void processBleResult(BleResultBean bean) {
        if(bean.getCMD() == BleProtocolState.CMD_KNOCK_DOOR_AND_UNLOCK_TIME) {
            if(bean.getPayload()[0] == 0) {
                // 设置敲击开锁成功
                if(mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                    mBleBean.getOKBLEDeviceImp().disConnect(false);
                }
            }
        }
    }


    private final CountDownTimer mCountDownTimer = new CountDownTimer(600000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(mBleBean != null) {
                if(!isRestartConnectingBle) {

                    mCountDownTimer.cancel();
                }
            }
        }

        @Override
        public void onFinish() {
            isRestartConnectingBle = false;
            if(mBleBean != null && mBleBean.getOKBLEDeviceImp() != null) {
                mBleBean.getOKBLEDeviceImp().disConnect(false);
            }
        }
    };

}
