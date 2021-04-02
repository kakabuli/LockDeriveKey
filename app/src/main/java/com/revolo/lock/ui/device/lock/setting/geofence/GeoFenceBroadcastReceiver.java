package com.revolo.lock.ui.device.lock.setting.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.JsonSyntaxException;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleCommandFactory;
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

}
