package com.revolo.lock.manager.geo;

import android.app.PendingIntent;

import androidx.room.ColumnInfo;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.device.lock.setting.geofence.GeoFenceHelper;

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
}
