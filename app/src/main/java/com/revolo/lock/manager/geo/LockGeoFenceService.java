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
import com.revolo.lock.Constant;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.manager.ble.BleManager;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.GeoFenceUnlockActivity;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceHelper;
import com.revolo.lock.ui.device.lock.setting.geofence.NotificationHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import timber.log.Timber;

public class LockGeoFenceService extends Service {
    public static final int MSG_LOCK_GEO_FEN_SERIVE_UPDATE = 589;
    private GeofencingClient mGeoFencingClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private String GEO_FENCE_ID = "SOME_GEO_FENCE_ID";
    private List<LockGeoFenceEn> lockGeoFenceEns;
    private Handler mHandler;

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
        //地理围栏服务初始化添加地理围栏设备
        addBleDevice();

    }

    public List<LockGeoFenceEn> getLockGeoFenceEns() {
        return lockGeoFenceEns;
    }

    /**
     * 获取当前地理围栏的个数
     *
     * @return
     */
    public int getLockGeoFencesLen() {
        if (null == lockGeoFenceEns) {
            return 0;
        } else {
            return lockGeoFenceEns.size();
        }
    }

    /**
     * 获取当前地理围栏状态
     *
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
     * 更新设备广播时间
     *
     * @param sn
     * @param time
     */
    public void setFenceTime(String sn, int time) {
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    lockGeoFenceEns.get(i).getBleDeviceLocal().setSetElectricFenceTime(time);
                    break;
                }
            }
        }
    }

    /**
     * 获取地理围栏连接ble次数
     *
     * @param sn
     * @return
     */
    public int getConnectBleIndex(String sn) {
        int index = -1;
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    index = lockGeoFenceEns.get(i).getConnectBleIndex();
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 设置地理围栏连接ble次数
     *
     * @param sn
     * @param number
     */
    public void setConnectBleIndex(String sn, int number) {

        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    lockGeoFenceEns.get(i).setConnectBleIndex(number);
                    break;
                }
            }
        }
    }

    /**
     * 获取地理围栏发送敲门开锁命令次数
     *
     * @param sn
     * @return
     */
    public int getOpenLockIndex(String sn) {
        int index = -1;
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    index = lockGeoFenceEns.get(i).getSendOpenLockIndex();
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 设置地理围栏敲门开锁命令的次数
     *
     * @param sn
     * @param number
     */
    public void setOpenLockIndex(String sn, int number) {

        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal() && lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(sn)) {
                    lockGeoFenceEns.get(i).setSendOpenLockIndex(number);
                    break;
                }
            }
        }
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

    /**
     * 添加定位
     */
    private void startLoaction() {
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

                    Timber.e("当前设备更新定位距离，更新相应的地理围栏数据");
                    if (null != App.getInstance().getLockAppService()) {
                        if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                            if (lockGeoFenceEns.get(i).getDistance() > 200 && results[0] < 200) {
                                Timber.e("历史记录距离大于200，最新的距离小于200，判定从地理围栏外围进入内部：" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                if (lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(true)) {
                                    GeoFenceMessage(true);
                                    Timber.e("当前设备定位距离小于200米，更新相应的地理围栏数据：" + lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceState());
                                    App.getInstance().getLockAppService().pushServiceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal());
                                    App.getInstance().getLockAppService().updateDeviceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac(),
                                            lockGeoFenceEns.get(i).getBleDeviceLocal());
                                }
                            } else if (lockGeoFenceEns.get(i).getDistance() < 200 && results[0] > 200) {
                                Timber.e("历史记录距离小于200，最新的距离大于200，判定从地理围栏内围出外部：" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                if (lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(false)) {
                                    GeoFenceMessage(false);
                                    Timber.e("当前设备定位距离大于200米，更新相应的地理围栏数据：" + lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceState());
                                    App.getInstance().getLockAppService().pushServiceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal());
                                    App.getInstance().getLockAppService().updateDeviceGeoState(lockGeoFenceEns.get(i).getBleDeviceLocal().getMac(),
                                            lockGeoFenceEns.get(i).getBleDeviceLocal());
                                }
                            }
                        }
                    }
                    //保存当前的相距地理围栏的距离
                    lockGeoFenceEns.get(i).setDistance((int) results[0]);
                    if (results[0] < 100) {
                        Timber.e("当前设备为100米内，准备发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                        if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                            if (!lockGeoFenceEns.get(i).getBleDeviceLocal().getElecFenceState()) {
                                Timber.e("当前设备非为从200米外进入设备，暂不发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                distance = distance < ((int) results[0] + 200) ? distance : ((int) results[0] + 200);
                            } else {
                                Timber.e("距离少于100米，开始发送命令:" + lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn());
                                pushMessage(lockGeoFenceEns.get(i));
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
                    Timber.e("当前距离:" + distance);
                    if (distance > 10000) {
                        if (lockGeoget.threadSleep != 2 * 60 * 60) {
                            lockGeoget.threadSleep = 2 * 60 * 60;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间两小时");
                        }
                    } else if (distance < 10000 && distance > 5000) {
                        if (lockGeoget.threadSleep != 60 * 60) {
                            lockGeoget.threadSleep = 60 * 60;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间一小时");
                        }
                    } else if (distance < 5000 && distance > 2000) {
                        if (lockGeoget.threadSleep != 30 * 60) {
                            lockGeoget.threadSleep = 30 * 60;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间三十分钟");
                        }
                    } else if (distance > 500 && distance < 2000) {
                        if (lockGeoget.threadSleep != 4 * 60) {
                            lockGeoget.threadSleep = 4 * 60;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间四分钟");
                        }
                    } else if (distance > 200 && distance < 500) {
                        if (lockGeoget.threadSleep != 60) {
                            lockGeoget.threadSleep = 60;
                            Timber.e("距离：%s", distance + "米" + "设置间隔时间一分钟");
                        }
                    } else if (distance > 50 && distance < 200) {
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

            }
        }
    }

    /**
     * 地理围栏进圈出圈通知
     *
     * @param isInto true 进圈 ； false 出圈
     */
    private void GeoFenceMessage(boolean isInto) {
        Timber.i("******************   %1s   ******************", isInto ? "进圈" : "出圈");
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        notificationHelper.sendHighPriorityNotification("Notice", isInto ? "The location is in range" : "The location is out of range", GeoFenceUnlockActivity.class);
    }

    /**
     * 分场景下发敲门开锁命令
     *
     * @param geoFenceEn
     */
    private void pushMessage(LockGeoFenceEn geoFenceEn) {
        //电子围栏是否开启
        BleDeviceLocal deviceLocal = geoFenceEn.getBleDeviceLocal();
        Timber.e("定位服务，下发命令");
        if (deviceLocal.isOpenElectricFence()) {
            Timber.e("定位服务，下发命令，设备地理围栏开启：" + deviceLocal.getEsn());
            if (null != App.getInstance().getLockAppService()) {
                BleBean bleBean = App.getInstance().getLockAppService().getUserBleBean(deviceLocal.getMac());
                if (null != bleBean) {
                    if (bleBean.getBleConning() == 2) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            int index = getOpenLockIndex(deviceLocal.getEsn());
                            if (index < 3) {
                                setOpenLockIndex(deviceLocal.getEsn(), index + 1);
                                if (bleBean == null) {
                                    Timber.e("mOnBleDeviceListener bleBean == null");
                                    return;
                                }
                                // TODO: 2021/4/7 抽离0x01
                                Timber.e("定位服务，下发命令，蓝牙下发敲门开锁命令：" + deviceLocal.getEsn());
                                BleManager.getInstance().writeControlMsg(BleCommandFactory
                                        .setKnockDoorAndUnlockTime(0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());
                            } else {
                                Timber.e("1当前发送敲门开锁命令的次数超过3次，当初始化地理围栏中设备数据");
                                clearBleDeviceMac(deviceLocal.getMac().toUpperCase());
                            }

                        }, 200);
                        return;
                    } else {
                        if (geoFenceEn.getElecFenceCmd() == 1) {
                            //开启蓝牙广播已，直接去连
                            Timber.e("定位服务，下发命令，开启蓝牙广播已，直接去连：" + deviceLocal.getEsn());
                            int index = getConnectBleIndex(deviceLocal.getEsn());
                            if (index < Constant.LOCK_GEO_CONNECT_BLE_INDEX) {
                                setConnectBleIndex(deviceLocal.getEsn(), index + 1);
                                App.getInstance().getLockAppService().checkBleConnect(deviceLocal.getMac());
                            } else {
                                Timber.e("1当前连接蓝牙的次数超过3次，当初始化地理围栏中设备数据");
                                clearBleDeviceMac(deviceLocal.getMac().toUpperCase());
                            }
                            //addDeviceScan(deviceLocal.getMac(), deviceLocal.getSetElectricFenceTime());
                            return;
                        }
                    }
                } else {
                    if (geoFenceEn.getElecFenceCmd() == 1) {
                        //开启蓝牙广播已，直接去连
                        int index = getConnectBleIndex(deviceLocal.getEsn());
                        if (index < Constant.LOCK_GEO_CONNECT_BLE_INDEX) {
                            setConnectBleIndex(deviceLocal.getEsn(), index + 1);
                            Timber.e("2定位服务，下发命令，开启蓝牙广播已，直接去连：" + deviceLocal.getEsn());
                            App.getInstance().getLockAppService().checkBleConnect(deviceLocal.getMac());
                        } else {
                            Timber.e("2当前连接蓝牙的次数超过3次，当初始化地理围栏中设备数据");
                            clearBleDeviceMac(deviceLocal.getMac().toUpperCase());
                        }
                        //addDeviceScan(deviceLocal.getMac(), deviceLocal.getSetElectricFenceTime());
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
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().isOpenElectricFence()) {
                        index = i;
                        break;
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

                    try {
                        Thread.sleep(3000);
                        Timber.e("定位服务，线程开始睡眠：" + threadSleep);
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
        startGeo();
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
            if (lockGeoFenceEns.size() < 1) {
                stopGeo();
            }
        } else {
            stopGeo();
        }
    }

    /**
     * 临时取消地理围栏中数据，获取定位消息继续
     *
     * @param mac
     */
    public void clearBleDeviceMac(String mac) {
        Timber.e("清理电子围栏：" + mac);
        if (null != lockGeoFenceEns) {
            for (int i = 0; i < lockGeoFenceEns.size(); i++) {
                if (null != lockGeoFenceEns.get(i).getBleDeviceLocal()) {
                    if (lockGeoFenceEns.get(i).getBleDeviceLocal().getMac().equals(mac) ||
                            lockGeoFenceEns.get(i).getBleDeviceLocal().getEsn().equals(mac)) {
                        Timber.e("定位服务，更新从发送开锁命令 mac：%s", mac + "状态：false");
                        lockGeoFenceEns.get(i).setElecFenceCmd(0);
                        Timber.e("定位服务，更新从两百米进入 mac：%s", mac + "状态：false");
                        lockGeoFenceEns.get(i).getBleDeviceLocal().setLockElecFenceState(false);
                        lockGeoFenceEns.get(i).setSendOpenBleIndex(0);
                        lockGeoFenceEns.get(i).setConnectBleIndex(0);
                        lockGeoFenceEns.get(i).setSendOpenLockIndex(0);
                    }
                }
            }

        }
    }

    /**
     * 完全清理掉地理围栏
     */
    public void clearBleDevice() {
        Timber.e("清理全部电子围栏");
        if (null != lockGeoFenceEns) {
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
                        LatLng latLng = new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLatitude());
                        lockGeoFenceEns.get(i).setBleDeviceLocal(bleDeviceLocal);
                        lockGeoFenceEns.get(i).setGeofence(lockGeoFenceEns.get(i).getmGeoFenceHelper().getGeoFence(
                                GEO_FENCE_ID, latLng
                                , 200,
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
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 200);
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
                lockGeoFenceEns.get(i).setElecFenceCmd(type);
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
                addGeoFence(bleDeviceLocal, new LatLng(bleDeviceLocal.getLatitude(), bleDeviceLocal.getLongitude()), 200);
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
                .addOnSuccessListener(aVoid -> {
                            Timber.d("onSuccess: GeoFence Added........");
                            startLoaction();
                        }
                )
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
                .addOnSuccessListener(aVoid -> {
                    Timber.d("onSuccess: GeoFence Added........");
                    startLoaction();
                })
                .addOnFailureListener(e -> {
                    // String errorMessage = mGeoFenceHelper.getErrorString(e);
                    Timber.d("GeoFence onFailure");
                });
    }

    /*private Handler lockGeoHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.arg1 == 200) {
                Timber.e("设备搜索超时：" + (String) msg.obj);
                clearBleDeviceMac((String) msg.obj);
                clearDeviceScan((String) msg.obj);
            }
        }
    };*/
   /* private LockGeoScanThread.LockGeoScanLinstener linstener = new LockGeoScanThread.LockGeoScanLinstener() {
        @Override
        public void onBLEDeviceScan(BLEScanResult bleScanResult, int i) {
            if (null != App.getInstance().getLockAppService()) {
                App.getInstance().getLockAppService().checkBleConnect(bleScanResult.getMacAddress());
            }
        }
    };*/

    /**
     * 添加设备搜索
     *
     * @param mac
     */
  /*  public void addDeviceScan(String mac, int time) {
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
    }*/

    /**
     * 清理设备搜索
     *
     * @param mac
     */
   /* public void clearDeviceScan(String mac) {
        mac = mac.toUpperCase();
        Timber.e("清理设备搜索：" + mac);
        int index = LockGeoScanThread.getInstance().getDeviceMap(mac);
        if (index > -1) {
            lockGeoHandler.removeMessages(index);
        }
        LockGeoScanThread.getInstance().clearDevice(mac);
    }*/
}
