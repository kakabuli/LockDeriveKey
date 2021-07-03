package com.revolo.lock.manager.geo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ConvertUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.manager.LockAppService;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceBroadcastReceiver;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceHelper;
import com.revolo.lock.ui.device.lock.setting.geofence.MapActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import timber.log.Timber;

public class LockGeoFenceService extends Service implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    public static final int MSG_LOCK_GEO_FEN_SERIVE_UPDATE = 589;
    private GeofencingClient mGeoFencingClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private String GEO_FENCE_ID = "SOME_GEO_FENCE_ID";
    private List<LockGeoFenceEn> lockGeoFenceEns;
    private Handler mHandler;


    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public class lockBinder extends Binder {
        public LockGeoFenceService getService() {
            return LockGeoFenceService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new lockBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        lockGeoFenceEns = new ArrayList<>();
        Timber.e("init lock geofence service");
        Timber.e("init lock map");
        if (null == mGeoFencingClient) {
            Timber.e("init lock map2");
            mGeoFencingClient = LocationServices.getGeofencingClient(LockGeoFenceService.this);
        }
        if (null == fusedLocationClient) {
            Timber.e("init lock map3");
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(LockGeoFenceService.this);
        }
        startGeo();

    }


    public void setHandler(Handler handler) {
        mHandler = handler;
        if (null != fusedLocationClient) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Timber.e("start local");
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // Got last known location. In some rare situations this can be null.
                Timber.e("lock geo locaion");
                sendLoacl(location);
            });
        }
    }

    private void sendLoacl(Location location) {
        if (location != null) {
            if (null != mHandler) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mHandler.obtainMessage(MSG_LOCK_GEO_FEN_SERIVE_UPDATE, latLng).sendToTarget();
                Timber.e("SG_LOCK_GEO_FEN_SERIVE_UPDATE");
            }
            if (null != lockGeoFenceEns && lockGeoFenceEns.size() > 0) {
                for (LockGeoFenceEn fenceEn : lockGeoFenceEns) {
                    float[] results = new float[1];
                    Location.distanceBetween(fenceEn.getLatitude(), fenceEn.getLongitude(), location.getLatitude(), location.getLongitude(), results);
                    if (null != lockGeoget) {
                        Timber.e("当前时间间距：%s", lockGeoget.threadSleep + "");
                    }
                    if (results[0] > 500) {
                        if (null != lockGeoget) {
                            if (lockGeoget.threadSleep != 5 * 60)
                                lockGeoget.threadSleep = 5 * 60;
                            Timber.e("距离：%s", results[0] + "米" + "设置间隔时间5分钟");
                        }
                    }
                    if (results[0] < 500 && results[0] > 200) {
                        if (null != lockGeoget) {
                            if (lockGeoget.threadSleep != 1 * 60)
                                lockGeoget.threadSleep = 1 * 60;
                            Timber.e("距离：%s", results[0] + "米" + "设置间隔时间1分钟");
                        }
                    }
                    if (results[0] > 50 && results[0] < 200) {
                        if (null != lockGeoget) {
                            if (lockGeoget.threadSleep != 30)
                                lockGeoget.threadSleep = 30;
                            Timber.e("距离：%s", results[0] + "米" + "设置间隔时间30秒");
                        }
                    }
                    if (results[0] > 30 && results[0] < 50) {
                        if (null != lockGeoget) {
                            if (lockGeoget.threadSleep != 20)
                                lockGeoget.threadSleep = 20;
                            Timber.e("距离：%s", results[0] + "米" + "设置间隔时间20秒");
                        }
                    }
                    if (results[0] < 30) {
                        if (null != lockGeoget) {
                            if (lockGeoget.threadSleep != 15)
                                lockGeoget.threadSleep = 15;
                            Timber.e("距离：%s", results[0] + "米" + "设置间隔时间15秒");
                        }
                    }
                    if (lockGeoget.threadSleep < 21) {
                        if (null != fenceEn.getBleDeviceLocal()) {
                            Timber.e("距离少于30米，开始发送命令");
                            pushMessage(fenceEn.getBleDeviceLocal().getEsn(), fenceEn.getBleDeviceLocal().getSetElectricFenceTime());
                        }

                        //sendBroadcast(new Intent(this, GeoFenceBroadcastReceiver.class));
                    }
                }
            }

            // Logic to handle location object
            //location.getLatitude(), location.getLongitude()
        }
    }

    private void pushMessage(String wifiID, int broadcastTime) {
        BleDeviceLocal deviceLocal = App.getInstance().getBleDeviceLocal();
        if (deviceLocal == null) {
            Timber.e("publishApproachOpen deviceLocal == null");
            return;
        }
        LockMessage lockMessage = new LockMessage();
        lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(wifiID, broadcastTime,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(deviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(deviceLocal.getPwd2()))));
        lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        lockMessage.setMessageType(2);
        lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
        EventBus.getDefault().post(lockMessage);

    }

    private LockGeoget lockGeoget;

    private void startGeo() {
        stopGeo();
        if (null != lockGeoFenceEns && lockGeoFenceEns.size() > 0) {
            Timber.e("开始电子围栏线程");
            lockGeoget = new LockGeoget();
            lockGeoget.start();
        }
    }

    private void stopGeo() {
        if (null != lockGeoget) {
            Timber.e("结束电子围栏线程");
            lockGeoget.isRun = false;
            lockGeoget.interrupt();
            lockGeoget = null;
        }
    }

    private class LockGeoget extends Thread {
        public boolean isRun = true;
        public int threadSleep = 1 * 60;

        @Override

        public void run() {

            while (isRun) {
                if (null != lockGeoFenceEns && lockGeoFenceEns.size() > 0) {
                    if (null != fusedLocationClient) {
                        if (ActivityCompat.checkSelfPermission(App.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(App.getInstance().getApplicationContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Timber.e("start local");
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            // Got last known location. In some rare situations this can be null.
                            Timber.e("lock geo locaion");
                            sendLoacl(location);
                        });
                    }
                    Timber.e("threadSleep: %s", threadSleep);
                    try {
                        Thread.sleep(threadSleep * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    /**
     * 开始添加
     */
    public void addBleDevice() {
        if (null == lockGeoFenceEns) {
            lockGeoFenceEns = new ArrayList<>();
        }
        Timber.e("start get list bledevicelocal");
        //获取当前的绑定设备
        List<BleDeviceLocal> bleDeviceLocalList = App.getInstance().getDeviceLists();
        if (null != bleDeviceLocalList) {
            Timber.e("start get list bledevicelocal size：%s", bleDeviceLocalList.size() + "");
            for (BleDeviceLocal bleDeviceLocal : bleDeviceLocalList) {
                Timber.e("check device");
                updateDeviceGeo(bleDeviceLocal);
            }
        }
    }

    /**
     * 清理
     *
     * @param esn
     */
    public void clearBleDevice(String esn) {
        Timber.e("清理电子围栏：" + esn);
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(esn)) {
                        lockGeoFenceEns.remove(i);
                    }
                }
            }
        }
        if (lockGeoFenceEns.size() < 1) {
            stopGeo();
        }
    }

    /**
     * 更新
     *
     * @param bleDeviceLocal
     */
    private void updateDeviceGeo(BleDeviceLocal bleDeviceLocal) {
        int index = -1;
        Timber.e("电子围栏中的个数:%s", lockGeoFenceEns.size() + "");
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(bleDeviceLocal.getEsn())) {
                index = i;
                if (!bleDeviceLocal.isOpenElectricFence()) {
                    Timber.e("电子围栏中状态:%s", bleDeviceLocal.isOpenElectricFence());
                    Timber.e("电子围栏中删除:%s", bleDeviceLocal.getEsn());
                    if (null != lockGeoFenceEns.get(i).getPendingIntent()) {
                        if (null != mGeoFencingClient) {
                            mGeoFencingClient.removeGeofences(lockGeoFenceEns.get(i).getPendingIntent());
                        }
                    }
                    lockGeoFenceEns.remove(i);
                    if (lockGeoFenceEns.size() < 1) {
                        stopGeo();
                    }
                    break;
                } else {
                    if (lockGeoFenceEns.get(i).getLatitude() == bleDeviceLocal.getLatitude() && lockGeoFenceEns.get(i).getLongitude() == bleDeviceLocal.getLongitude()) {
                        //重复
                        Timber.e("电子围栏中重复:%s", bleDeviceLocal.getEsn());
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                    } else {
                        lockGeoFenceEns.get(i).setLatitude(bleDeviceLocal.getLatitude());
                        lockGeoFenceEns.get(i).setLongitude(bleDeviceLocal.getLongitude());
                        LatLng latLng = new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLockPower());
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                        lockGeoFenceEns.get(i).setGeofence(lockGeoFenceEns.get(i).getmGeoFenceHelper().getGeoFence(
                                GEO_FENCE_ID, latLng
                                , 50,
                                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT
                        ));
                        lockGeoFenceEns.get(i).setGeofencingRequest(lockGeoFenceEns.get(i).getmGeoFenceHelper().getGeoFencingRequest(lockGeoFenceEns.get(i).getGeofence()));
                        addGeoFence(lockGeoFenceEns.get(i));
                        Timber.e("电子围栏中重复，修改坐标点:%s", bleDeviceLocal.getEsn());

                    }

                    break;
                }

            }
        }
        if (index == -1) {
            if (bleDeviceLocal.isOpenElectricFence()) {
                Timber.e("start get list bledevicelocal addGeo mac：%s", bleDeviceLocal.getMac() + ";" + bleDeviceLocal.getEsn());
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 50);
            }
        }
    }

    /**
     * 更新
     *
     * @param bleDeviceLocal
     */
    public void updateDeviceGeo(BleDeviceLocal bleDeviceLocal, LatLng latLng, float radius) {
        int index = -1;
        Timber.e("电子围栏中的个数:%s", lockGeoFenceEns.size() + "");
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(bleDeviceLocal.getEsn())) {
                index = i;
                if (!bleDeviceLocal.isOpenElectricFence()) {
                    Timber.e("电子围栏中状态:%s", bleDeviceLocal.isOpenElectricFence());
                    Timber.e("电子围栏中删除:%s", bleDeviceLocal.getEsn());
                    if (null != lockGeoFenceEns.get(i).getPendingIntent()) {
                        if (null != mGeoFencingClient) {
                            mGeoFencingClient.removeGeofences(lockGeoFenceEns.get(i).getPendingIntent());
                        }
                    }
                    lockGeoFenceEns.remove(i);
                    if (lockGeoFenceEns.size() < 1) {
                        stopGeo();
                    }
                    break;
                } else {
                    if (lockGeoFenceEns.get(i).getLatitude() == latLng.latitude && lockGeoFenceEns.get(i).getLongitude() == latLng.longitude) {
                        //重复
                        Timber.e("电子围栏中重复:%s", bleDeviceLocal.getEsn());
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                    } else {
                        lockGeoFenceEns.get(i).setLatitude(latLng.latitude);
                        lockGeoFenceEns.get(i).setLongitude(latLng.longitude);
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                        lockGeoFenceEns.get(i).setGeofence(lockGeoFenceEns.get(i).getmGeoFenceHelper().getGeoFence(
                                GEO_FENCE_ID, latLng
                                , radius,
                                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT
                        ));
                        lockGeoFenceEns.get(i).setGeofencingRequest(lockGeoFenceEns.get(i).getmGeoFenceHelper().getGeoFencingRequest(lockGeoFenceEns.get(i).getGeofence()));
                        addGeoFence(lockGeoFenceEns.get(i));
                        Timber.e("电子围栏中重复，修改坐标点:%s", bleDeviceLocal.getEsn());

                    }

                    break;
                }

            }
        }
        if (index == -1) {
            if (bleDeviceLocal.isOpenElectricFence()) {
                Timber.e("start get list bledevicelocal addGeo mac：%s", bleDeviceLocal.getMac() + ";" + bleDeviceLocal.getEsn());
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 50);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeoFence(BleDeviceLocal bleDeviceLocal, LatLng latLng, float radius) {
        Timber.d("addGeoFence: started");
        LockGeoFenceEn fenceEn = new LockGeoFenceEn();
        fenceEn.setLatitude(latLng.latitude);
        fenceEn.setLongitude(latLng.longitude);
        fenceEn.setBleDeviceLocal(bleDeviceLocal);
        fenceEn.setmGeoFenceHelper(new GeoFenceHelper(this));
        fenceEn.setGeofence(fenceEn.getmGeoFenceHelper().getGeoFence(
                GEO_FENCE_ID,
                latLng,
                radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT
        ));
        fenceEn.setGeofencingRequest(fenceEn.getmGeoFenceHelper().getGeoFencingRequest(fenceEn.getGeofence()));

        fenceEn.setPendingIntent(fenceEn.getmGeoFenceHelper().getPendingIntent());
        lockGeoFenceEns.add(fenceEn);
        startGeo();
        mGeoFencingClient.addGeofences(fenceEn.getGeofencingRequest(), fenceEn.getPendingIntent())
                .addOnSuccessListener(aVoid -> Timber.d("onSuccess: GeoFence Added........"))
                .addOnFailureListener(e -> {
                    // String errorMessage = mGeoFenceHelper.getErrorString(e);
                    Timber.d("GeoFence onFailure");
                });

    }

    /**
     * 添加电子围栏
     *
     * @param fenceEn
     */
    public void addGeoFence(LockGeoFenceEn fenceEn) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGeoFencingClient.removeGeofences(fenceEn.getPendingIntent());
        mGeoFencingClient.addGeofences(fenceEn.getGeofencingRequest(), fenceEn.getPendingIntent())
                .addOnSuccessListener(aVoid -> Timber.d("onSuccess: GeoFence Added........"))
                .addOnFailureListener(e -> {
                    // String errorMessage = mGeoFenceHelper.getErrorString(e);
                    Timber.d("GeoFence onFailure");
                });
    }

}
