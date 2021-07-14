package com.revolo.lock.ui.device.lock.setting.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.geo.LockGeoFenceService;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GoogleMap mMap;
    public float GEO_FENCE_RADIUS = 50;
    private BleDeviceLocal mBleDeviceLocal;
    private SelectDialog canApplyDialog, refuseDialog;
    private RelativeLayout addLocation;
    private LatLng mCurrLat = null;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            Timber.e("initData mBleDeviceLocal == null");
            return;
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_map;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == LockGeoFenceService.MSG_LOCK_GEO_FEN_SERIVE_UPDATE) {
                LatLng location = (LatLng) msg.obj;
                if (location != null) {
                    // Logic to handle location object
                    if (mMap != null) {
                        LatLng dhaka = new LatLng(location.latitude, location.longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, 16));
                        if (mBleDeviceLocal.isOpenElectricFence()) {
                            double la = mBleDeviceLocal.getLatitude();
                            double lo = mBleDeviceLocal.getLongitude();
                            if (mMap != null) {
                                mMap.clear();
                                LatLng latLng = new LatLng(la, lo);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                                handleMapLongClick(latLng);
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setStatusBarColor(R.color.white);
        addLocation = findViewById(R.id.map_activity_add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mCurrLat) {
                    saveLatLngToLocal(mCurrLat);
                }
            }
        });
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        if (null != App.getInstance().getLockGeoFenceService()) {
            App.getInstance().getLockGeoFenceService().setHandler(handler);
        }
    }

    /**
     * 可申请蓝牙状态
     */
    private void updateApplyDialog() {
        if (null == canApplyDialog) {
            canApplyDialog = new SelectDialog(this);
            canApplyDialog.setMessage(getString(R.string.dialog_we_need_to_permission_for_location));
            canApplyDialog.setOnCancelClickListener(v -> canApplyDialog.dismiss());
            canApplyDialog.setOnConfirmListener(v -> {
                canApplyDialog.dismiss();
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            });
            if (!canApplyDialog.isShowing())
                canApplyDialog.show();
        }
    }

    /**
     * 申请权限拒绝提示
     */
    private void refuseDialog() {
        if (null == refuseDialog) {
            refuseDialog = new SelectDialog(this);
            refuseDialog.setMessage(getString(R.string.dialog_go_to_settings_enable_this_permission));
            refuseDialog.setOnCancelClickListener(v -> refuseDialog.dismiss());
            refuseDialog.setOnConfirmListener(v -> {
                refuseDialog.dismiss();
                gotoApplicationSettings();
            });
            if (!refuseDialog.isShowing())
                refuseDialog.show();
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
    protected void onDestroy() {
        super.onDestroy();
        if (null != App.getInstance().getLockGeoFenceService()) {
            App.getInstance().getLockGeoFenceService().setHandler(null);
        }
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
       /* if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
                saveLatLngToLocal(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        } else {
            handleMapLongClick(latLng);
            saveLatLngToLocal(latLng);
        }*/
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show a dialog for displaying why the permission is needed and the ask the permission
                updateApplyDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We have the permission
                mMap.setMyLocationEnabled(true);
            } else {
                //Permission is not Granted
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //This block here means PERMANENTLY DENIED PERMISSION
                    refuseDialog();
                }
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_you_can_add_geo_fences);
            } else {
                //We do not have the permission..
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_bg_loc_access_is_necessary_for_geo_fences_to_trigger);
            }
        }
    }

    private void handleMapLongClick(LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEO_FENCE_RADIUS);
        mCurrLat=latLng;
    }

    private void saveLatLngToLocal(@NotNull LatLng latLng) {
        addGeoFence(latLng, GEO_FENCE_RADIUS);
        mBleDeviceLocal.setLatitude(latLng.latitude);
        mBleDeviceLocal.setLongitude(latLng.longitude);
        mBleDeviceLocal.setOpenElectricFence(true);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    @SuppressLint("MissingPermission")
    private void addGeoFence(LatLng latLng, float radius) {
        Timber.d("addGeoFence: started");
        if (null != App.getInstance().getLockGeoFenceService()) {
            mBleDeviceLocal.setOpenElectricFence(true);
            if(App.getInstance().getLockGeoFenceService().updateDeviceGeo(mBleDeviceLocal, latLng, radius)){
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.geofence_added_successfully);
            }else{
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.failed_to_add_geofence);
            }
        }
    }
    /**
     * 更新锁服务器存储的数据
     */
    private void updateLockInfoToService(LatLng latLng) {
        if (App.getInstance().getUserBean() == null) {
            Timber.e("updateLockInfoToService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("updateLockInfoToService uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateLockInfoToService token is empty");
            return;
        }
        showLoading();

        UpdateLockInfoReq req = new UpdateLockInfoReq();
        req.setSn(mBleDeviceLocal.getEsn());
        req.setWifiName(mBleDeviceLocal.getConnectedWifiName());
        req.setSafeMode(0);   // 没有使用这个
        req.setLanguage("en"); // 暂时也没使用这个
        req.setVolume(mBleDeviceLocal.isMute() ? 1 : 0);
        req.setAmMode(mBleDeviceLocal.isAutoLock() ? 0 : 1);
        req.setDuress(mBleDeviceLocal.isDuress() ? 0 : 1);
        req.setMagneticStatus(mBleDeviceLocal.getDoorSensor());
        req.setDoorSensor(mBleDeviceLocal.isOpenDoorSensor() ? 1 : 0);
        req.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 0 : 1);
        req.setAutoLockTime(mBleDeviceLocal.getSetAutoLockTime());
        req.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
        req.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
        Timber.e("std44555554445:%s",
                req.toString());
        Observable<UpdateLockInfoRsp> observable = HttpRequest.getInstance().updateLockInfo(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateLockInfoRsp>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull UpdateLockInfoRsp updateLockInfoRsp) {
                dismissLoading();
                String code = updateLockInfoRsp.getCode();
                if (!code.equals("200")) {
                    String msg = updateLockInfoRsp.getMsg();
                    Timber.e("updateLockInfoToService code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg))
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                }
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

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();

        circleOptions.center(latLng)
                .radius(radius)
                .strokeColor(Color.argb(255, 255, 0, 0))
                .fillColor(Color.argb(64, 255, 0, 0))
                .strokeWidth(4);

        mMap.addCircle(circleOptions);
    }

    private void gotoApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        } else {
            handleMapLongClick(latLng);
        }
    }
}
