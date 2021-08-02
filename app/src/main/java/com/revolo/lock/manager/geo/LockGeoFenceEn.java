package com.revolo.lock.manager.geo;

import android.app.PendingIntent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceHelper;

import timber.log.Timber;

/**
 * 定位实体类
 */
public class LockGeoFenceEn {
    private BleDeviceLocal bleDeviceLocal;
    private GeoFenceHelper mGeoFenceHelper;
    private Geofence geofence;
    private GeofencingRequest geofencingRequest;
    private PendingIntent pendingIntent;
    private double latitude;                                          // 地理围栏纬度
    private double longitude;
    private int distance = 0;//距离
    private int elecFenceCmd;//是否发送MQTT命令
    private int sendOpenBleIndex = 0;//发送开启蓝牙广播的次数 ，最多不过3次
    private int connectBleIndex = 0;//连接ble的次数不超过3次
    private int sendOpenLockIndex = 0;//发送敲门开锁命令的次数

    public BleDeviceLocal getBleDeviceLocal() {
        return bleDeviceLocal;
    }

    public void setBleDeviceLocal(BleDeviceLocal bleDeviceLocal) {
        this.bleDeviceLocal = bleDeviceLocal;
    }

    public GeoFenceHelper getmGeoFenceHelper() {
        return mGeoFenceHelper;
    }

    public void setmGeoFenceHelper(GeoFenceHelper mGeoFenceHelper) {
        this.mGeoFenceHelper = mGeoFenceHelper;
    }

    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    public GeofencingRequest getGeofencingRequest() {
        return geofencingRequest;
    }

    public void setGeofencingRequest(GeofencingRequest geofencingRequest) {
        this.geofencingRequest = geofencingRequest;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getSendOpenBleIndex() {
        return sendOpenBleIndex;
    }

    public void setSendOpenBleIndex(int sendOpenBleIndex) {
        this.sendOpenBleIndex = sendOpenBleIndex;
    }

    public int getConnectBleIndex() {
        return connectBleIndex;
    }

    public void setConnectBleIndex(int connectBleIndex) {
        this.connectBleIndex = connectBleIndex;
    }

    public int getSendOpenLockIndex() {
        return sendOpenLockIndex;
    }

    public void setSendOpenLockIndex(int sendOpenLockIndex) {
        this.sendOpenLockIndex = sendOpenLockIndex;
    }
    public int getElecFenceCmd() {
        return elecFenceCmd;
    }

    public void setElecFenceCmd(int elecFenceCmd) {
        Timber.e("kdjakjgkdjkjakjgkdjkfjksjkdjkjask ElecFenceCmd:"+elecFenceCmd);
        this.elecFenceCmd = elecFenceCmd;
    }

}
