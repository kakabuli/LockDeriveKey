package com.revolo.lock.manager.geo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.a1anwang.okble.client.scan.BLEScanResult;
import com.blankj.utilcode.util.ConvertUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.revolo.lock.App;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.manager.ble.BleManager;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

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
        Timber.e("定位服务初始化");
        if (null == mGeoFencingClient) {
            mGeoFencingClient = LocationServices.getGeofencingClient(LockGeoFenceService.this);
        }
        if (null == fusedLocationClient) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(LockGeoFenceService.this);
        }
        startLocal();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocal();
            }
        }, 10000);
        //地理围栏服务初始化添加地理围栏设备
        addBleDevice();
        //startGeo();

    }

    private void startLocal() {
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
            Timber.e("定位服务，设置msg，开始定位");
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // Got last known location. In some rare situations this can be null.
                Timber.e("定位服务，定位成功，next");
            });
        }
    }

    public List<LockGeoFenceEn> getLockGeoFenceEns() {
        return lockGeoFenceEns;
    }

    /**
     * 获取当前地理围栏状态
     * @param mac
     * @return
     */
    public BleDeviceLocal getLocakGeoFenceEn(String mac) {
        Timber.e("LocakGeoFenceEn");
        BleDeviceLocal bleDeviceLocal = null;
        if (null != lockGeoFenceEns) {
            for (LockGeoFenceEn fenceEn : lockGeoFenceEns) {
                if (null != fenceEn && null != fenceEn.getBleDeviceLocal()) {
                    if (fenceEn.getBleDeviceLocal().getMac().equals(mac)) {
                        bleDeviceLocal = fenceEn.getBleDeviceLocal();
                        break;
                    }
                }
            }
        }
        return bleDeviceLocal;
    }

    /**
     * 获取地理围栏开启蓝牙广播发送次数
     *
     * @param sn
     * @return
     */
    public int getSendOpenBleIndex(String sn) {
        int index = -1;
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    index = lockGeoFenceEns.get(i).getSendOpenBleIndex();
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 设置地理围栏开启蓝牙广播发送次数
     *
     * @param sn
     * @param number
     */
    public void setSendOpenBleIndex(String sn, int number) {

        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    lockGeoFenceEns.get(i).setSendOpenBleIndex(number);
                    break;
                }
            }
        }
    }


    public void setHandler(Handler handler) {
        mHandler = handler;
        if (null == mHandler) {
            return;
        }
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
            Timber.e("定位服务，设置msg，开始定位");
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // Got last known location. In some rare situations this can be null.
                Timber.e("定位服务，定位成功，next");
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
                int distance = 600;
                for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                    float[] results = new float[1];
                    Location.distanceBetween(lockGeoFenceEns.get(i).getLatitude(), lockGeoFenceEns.get(i).getLongitude(), location.getLatitude(), location.getLongitude(), results);
                    if (null != lockGeoget) {
                        Timber.e("当前时间间距：%s", lockGeoget.threadSleep + "");
                    }
                    lockGeoFenceEns.get(i).setDistance((int) results[0]);
                    if (!lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceState()) {
                        Timber.e("当前设备非为从200米外进入设备，暂不发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                        if (results[0] > 200) {
                            Timber.e("当前设备定位距离大于200米，清理相应的地理围栏数据");
                            clearBleDeviceMac(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac());
                        /*if (null != App.getInstance().getLockAppService()) {
                            if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                                if (lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(true)) {
                                    App.getInstance().getLockAppService().pushServiceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal());
                                    App.getInstance().getLockAppService().updateDeviceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac(), lockGeoFenceEns.get(i).getBleDeviceLocal());
                                }
                            }
                        }*/
                        }
                    }
                    if (results[0] < 100) {
                        Timber.e("当前设备为100米内，准备发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                        if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                            if (!lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceState()) {
                                Timber.e("当前设备非为从200米外进入设备，暂不发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                distance = distance < ((int) results[0] + 200) ? distance : ((int) results[0] + 200);
                            } else {
                                Timber.e("距离少于100米，开始发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                pushMessage(lockGeoFenceEns.get(i).getBleDeviceLocal());
                                distance = distance < (int) results[0] ? distance : (int) results[0];
                            }
                        } else {
                            distance = distance < (int) results[0] ? distance : (int) results[0];
                        }
                    } else {
                        distance = distance < (int) results[0] ? distance : (int) results[0];
                    }
                }

                //////////////////////////////////////////////////////////////////
                if (null != lockGeoget) {
                    if (distance > 50 && distance < 200) {
                        if (lockGeoget.threadSleep != 30) {
                            lockGeoget.threadSleep = 30;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间30秒");
                        }
                    } else if (distance > 30 && distance < 50) {
                        if (lockGeoget.threadSleep != 20) {
                            lockGeoget.threadSleep = 20;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间20秒");
                        }
                    } else if (distance < 30) {

                        if (lockGeoget.threadSleep != 15) {
                            lockGeoget.threadSleep = 15;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间15秒");
                        }
                    }
                }
                startGeo();
            }
        }
    }

    /**
     * 分场景下发敲门开锁命令
     *
     * @param deviceLocal
     */
    private void pushMessage(BleDeviceLocal deviceLocal) {
        //电子围栏是否开启
        Timber.e("定位服务，下发命令");
        if (deviceLocal.isOpenElectricFence()) {
            Timber.e("定位服务，下发命令，设备地理围栏开启：" + deviceLocal.getEsn());
            if (null != App.getInstance().getLockAppService()) {
                BleBean bleBean = App.getInstance().getLockAppService().getUserBleBean(deviceLocal.getMac());
                if (null != bleBean) {
                    if (bleBean.getBleConning() == 2) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (bleBean == null) {
                                Timber.e("mOnBleDeviceListener bleBean == null");
                                return;
                            }
                            // TODO: 2021/4/7 抽离0x01
                            Timber.e("定位服务，下发命令，蓝牙下发敲门开锁命令：" + deviceLocal.getEsn());
                            BleManager.getInstance().writeControlMsg(BleCommandFactory
                                    .setKnockDoorAndUnlockTime(0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
                        }, 200);
                        return;
                    } else {
                        if (deviceLocal.getElecFenceCmd() == 1) {
                            //开启蓝牙广播已，直接去连
                            Timber.e("定位服务，下发命令，开启蓝牙广播已，直接去连：" + deviceLocal.getEsn());
                            //  App.getInstance().getLockAppService().checkBleConnect(deviceLocal.getMac());
                            addDeviceScan(deviceLocal.getMac(), deviceLocal.getSetElectricFenceTime());
                            return;
                        }
                    }
                } else {
                    if (deviceLocal.getElecFenceCmd() == 1) {
                        //开启蓝牙广播已，直接去连
                        Timber.e("定位服务，下发命令，开启蓝牙广播已，直接去连：" + deviceLocal.getEsn());
                        //  App.getInstance().getLockAppService().checkBleConnect(deviceLocal.getMac());
                        addDeviceScan(deviceLocal.getMac(), deviceLocal.getSetElectricFenceTime());
                        return;
                    }
                }
            }
            int index = getSendOpenBleIndex(deviceLocal.getEsn());
            if (index < 3) {
                setSendOpenBleIndex(deviceLocal.getEsn(), index + 1);
                Timber.e("定位服务，下发命令，mqtt命令开启蓝牙：" + deviceLocal.getEsn());
                LockMessage lockMessage = new LockMessage();
                lockMessage.setMqttMessage(MqttCommandFactory.approachOpen(deviceLocal.getEsn(), deviceLocal.getSetElectricFenceTime(),
                        BleCommandFactory.getPwd(
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd1()),
                                ConvertUtils.hexString2Bytes(deviceLocal.getPwd2())), 1, 2));
                lockMessage.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
                lockMessage.setMessageType(2);
                lockMessage.setMqtt_message_code(MQttConstant.APP_ROACH_OPEN);
                EventBus.getDefault().post(lockMessage);
            } else {
                Timber.e("当前开启蓝牙广播的次数超过3次，当初始化地理围栏中设备数据");
                clearBleDeviceMac(deviceLocal.getMac().toUpperCase());
            }
        }

    }

    private LockGeoget lockGeoget;

    public void startGeo() {
        stopGeo();
        if (null != lockGeoFenceEns) {
            int index = -1;
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i) && null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceCmd() == 1) {
                        index = i;
                    }
                }
            }
            if (index > -1) {
                Timber.e("开始电子围栏线程");
                lockGeoget = new LockGeoget();
                lockGeoget.start();
            }
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

   /* private void sleepGeo() {
        if (null != lockGeoget) {
            Timber.e("定位服务，线程开始睡眠：" + lockGeoget.threadSleep);
            try {
                lockGeoget.isAlive()
                lockGeoget.sleep(lockGeoget.threadSleep * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/

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
                        Timber.e("定位服务，线程开始定位");
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            // Got last known location. In some rare situations this can be null.
                            Timber.e("定位服务，线程定位成功");
                            sendLoacl(location);
                        });
                    }
                    Timber.e("定位服务，线程开始睡眠：" + threadSleep);
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
        Timber.e("lock geo service destroy");
    }


    /**
     * 开始添加
     */
    public void addBleDevice() {
        if (null == lockGeoFenceEns) {
            lockGeoFenceEns = new ArrayList<>();
        }
        Timber.e("定位服务，添加设备列表");
        //获取当前的绑定设备
        List<BleDeviceLocal> bleDeviceLocalList = App.getInstance().getDeviceLists();
        if (null != bleDeviceLocalList) {
            Timber.e("定位服务，添加设备列表len：" + bleDeviceLocalList.size());
            for (BleDeviceLocal bleDeviceLocal : bleDeviceLocalList) {
                if (bleDeviceLocal.isOpenElectricFence()) {
                    updateDeviceGeo(bleDeviceLocal);
                }
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
                        clearDeviceScan(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac());
                        lockGeoFenceEns.remove(i);
                    }
                }
            }
            if (lockGeoFenceEns.size() < 1) {
                stopGeo();
            }
        } else {
            stopGeo();
        }
    }

    /**
     * 根据mac清理电子围栏设备
     *
     * @param mac
     */
    public void clearBleDeviceMac(String mac) {
        Timber.e("清理电子围栏：" + mac);
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().getMac().equals(mac)) {
                        clearDeviceScan(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac());
                        Timber.e("定位服务，更新从发送开锁命令 mac：%s", mac + "状态：false");
                        lockGeoFenceEns.get(i).getBleDeviceLocal().setElecFenceCmd(0);
                        Timber.e("定位服务，更新从两百米进入 mac：%s", mac + "状态：false");
                        lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(false);
                    }
                }
            }
            if (lockGeoFenceEns.size() < 1) {
                stopGeo();
            } else {
                startGeo();
            }
        } else {
            stopGeo();
        }
    }

    /**
     * @param esn
     */
    public void clearDeviceS(String esn) {
        Timber.e("清理电子围栏搜索：" + esn);
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(esn)) {
                        clearDeviceScan(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac());
                    }
                }
            }
        }
    }

    public void clearBleDevice() {
        Timber.e("清理全部电子围栏");
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    clearDeviceScan(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac());
                }
            }
            lockGeoFenceEns.clear();
            stopGeo();
        } else {
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
        Timber.e("定位服务，地理围栏设备列表len：" + lockGeoFenceEns.size());
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(bleDeviceLocal.getEsn())) {
                index = i;
                if (!bleDeviceLocal.isOpenElectricFence()) {
                    Timber.e("定位服务，地理围栏设备状态:%s", bleDeviceLocal.isOpenElectricFence());
                    Timber.e("定位服务，地理围栏设备删除:%s", bleDeviceLocal.getEsn());
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
                        Timber.e("定位服务，地理围栏设备重复:%s", bleDeviceLocal.getEsn());
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                        sendOpenPer();
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
                        Timber.e("定位服务，地理围栏设备中重复，修改坐标点:%s", bleDeviceLocal.getEsn());
                        sendOpenPer();
                    }

                    break;
                }

            }
        }
        if (index == -1) {
            if (bleDeviceLocal.isOpenElectricFence()) {
                sendOpenPer();
                Timber.e("定位服务，地理围栏添加设备 mac：%s", bleDeviceLocal.getMac() + ";" + bleDeviceLocal.getEsn());
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 50);
            }
        }
    }

    /**
     * 记录发送开启蓝牙命令
     *
     * @param esn
     * @param type
     */
    public void updateLockCmdState(String esn, int type) {
        int index = -1;
        Timber.e("定位服务，地理围栏设备 len:%s", lockGeoFenceEns.size() + "");
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(esn)) {
                Timber.e("定位服务，更新发送开启蓝牙命令状态 mac：%s", esn + "状态：" + type);
                lockGeoFenceEns.get(i).getBleDeviceLocal().setElecFenceCmd(type);
                break;
            }
        }
    }

    /**
     * 更新从200米进入模式
     *
     * @param esn
     * @param type
     */
    public void updateLockLocalState(String esn, boolean type) {
        int index = -1;
        Timber.e("定位服务，地理围栏设备 len:%s", lockGeoFenceEns.size() + "");
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(esn)) {
                Timber.e("定位服务，更新从两百米进入 mac：%s", esn + "状态：" + type);
                lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(type);
                break;
            }
        }
    }

    /**
     * 申请打开权限
     */
    private void sendOpenPer() {
        LockMessageRes lockMessageRes = new LockMessageRes();
        lockMessageRes.setMessgaeType(LockMessageCode.MSG_LOCK_MESSAGE_USER);
        lockMessageRes.setResultCode(LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS);
        lockMessageRes.setMessageCode(LockMessageCode.MSG_LOCK_MESSAGE_OPEN_PERMISSION);
        EventBus.getDefault().post(lockMessageRes);
    }

    /**
     * 更新
     *
     * @param bleDeviceLocal
     */
    public boolean updateDeviceGeo(BleDeviceLocal bleDeviceLocal, LatLng latLng, float radius) {
        int index = -1;
        boolean isAdd = false;
        Timber.e("2定位服务，地理围栏设备个数:%s", lockGeoFenceEns.size() + "");
        for (int i = 0; i < lockGeoFenceEns.size(); i++) {
            if (lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(bleDeviceLocal.getEsn())) {
                index = i;
                if (!bleDeviceLocal.isOpenElectricFence()) {
                    Timber.e("2定位服务，地理围栏设备状态:%s", bleDeviceLocal.isOpenElectricFence());
                    Timber.e("2定位服务，地理围栏设备删除:%s", bleDeviceLocal.getEsn());
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
                        Timber.e("2定位服务，地理围栏设备重复:%s", bleDeviceLocal.getEsn());
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
                        Timber.e("2定位服务，地理围栏设备重复，修改坐标点:%s", bleDeviceLocal.getEsn());
                        isAdd = true;

                    }

                    break;
                }

            }
        }
        if (index == -1) {
            if (bleDeviceLocal.isOpenElectricFence()) {
                Timber.e("2定位服务，地理围栏添加设备 mac：%s", bleDeviceLocal.getMac() + ";" + bleDeviceLocal.getEsn());
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 50);
                isAdd = true;
            }
        }
        return isAdd;
    }

    @SuppressLint("MissingPermission")
    private void addGeoFence(BleDeviceLocal bleDeviceLocal, LatLng latLng, float radius) {
        Timber.d("定位服务，地理围栏 添加设备");
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

        fenceEn.setPendingIntent(fenceEn.getmGeoFenceHelper().getPendingIntent(bleDeviceLocal.getEsn()));
        lockGeoFenceEns.add(fenceEn);
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
        Timber.e("定位服务，地理围栏 重复添加坐标");
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

    private Handler lockGeoHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.arg1 == 200) {
                Timber.e("设备搜索超时：" + (String) msg.obj);
                clearBleDeviceMac((String) msg.obj);
                clearDeviceScan((String) msg.obj);
            }
        }
    };
    private LockGeoScanThread.LockGeoScanLinstener linstener = new LockGeoScanThread.LockGeoScanLinstener() {
        @Override
        public void onBLEDeviceScan(BLEScanResult bleScanResult, int i) {
            if (null != App.getInstance().getLockAppService()) {
                App.getInstance().getLockAppService().checkBleConnect(bleScanResult.getMacAddress());
            }
        }
    };

    /**
     * 添加设备搜索
     *
     * @param mac
     */
    public void addDeviceScan(String mac, int time) {
        mac = mac.toUpperCase();
        Timber.e("添加设备开始搜索：" + mac);
        int index = LockGeoScanThread.getInstance().addDevice(mac, linstener);
        if (index > -1) {
            Message msg = new Message();
            msg.what = index;
            msg.arg1 = 200;
            msg.obj = mac;
            lockGeoHandler.sendMessageDelayed(msg, time * 60 * 10000);
        }
    }

    /**
     * 清理设备搜索
     *
     * @param mac
     */
    public void clearDeviceScan(String mac) {
        mac = mac.toUpperCase();
        Timber.e("清理设备搜索：" + mac);
        int index = LockGeoScanThread.getInstance().getDeviceMap(mac);
        if (index > -1) {
            lockGeoHandler.removeMessages(index);
        }
        LockGeoScanThread.getInstance().clearDevice(mac);
    }
}
