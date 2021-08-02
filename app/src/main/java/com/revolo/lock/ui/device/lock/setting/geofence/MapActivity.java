package com.revolo.lock.ui.device.lock.setting.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.UpdateLocalBeanReq;
import com.revolo.lock.bean.respone.UpdateLocalBeanRsp;
import com.revolo.lock.dialog.PrivacyPolicyDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.geo.LockGeoFenceService;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.MainActivity;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;

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
    public float GEO_FENCE_RADIUS = 200;
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

    private void onPrivacyPolicyDialog() {
        PrivacyPolicyDialog privacyPolicyDialog = new PrivacyPolicyDialog(this);
        privacyPolicyDialog.setOnConfirmListener(v -> {
            privacyPolicyDialog.dismiss();
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        });
        privacyPolicyDialog.setOnCancelClickListener(v -> {
            SPUtils.getInstance(REVOLO_SP).put(Constant.FIRST_OPEN_APP, true);
            privacyPolicyDialog.dismiss();
        });
        privacyPolicyDialog.show();
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
                onPrivacyPolicyDialog();
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
        mCurrLat = latLng;
    }

    private void saveLatLngToLocal(@NotNull LatLng latLng) {
        addGeoFence(latLng, GEO_FENCE_RADIUS);
    }

    /**
     * 上传给服务器
     */
    private void pushService() {
        showLoading();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                UpdateLocalBeanReq lockLocal = new UpdateLocalBeanReq();
                lockLocal.setSn(mBleDeviceLocal.getEsn());
                lockLocal.setElecFence(mBleDeviceLocal.isOpenElectricFence() ? 1 : 0);
                lockLocal.setElecFenceSensitivity(mBleDeviceLocal.getSetElectricFenceSensitivity());
                lockLocal.setElecFenceTime(mBleDeviceLocal.getSetElectricFenceTime());
                lockLocal.setLatitude(mBleDeviceLocal.getLatitude() + "");
                lockLocal.setLongitude(mBleDeviceLocal.getLongitude() + "");
                lockLocal.setElecFenceState(mBleDeviceLocal.getElecFenceState() ? 0 : 1);
                String token = App.getInstance().getUserBean().getToken();
                Observable<UpdateLocalBeanRsp> observable = HttpRequest.getInstance().updateockeLecfence(token, lockLocal);
                ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateLocalBeanRsp>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UpdateLocalBeanRsp changeKeyNickBeanRsp) {
                        dismissLoading();
                        String code = changeKeyNickBeanRsp.getCode();
                        if (TextUtils.isEmpty(code)) {
                            Timber.e("changeKeyNickBeanRsp.getCode() is Empty");
                            return;
                        }
                        String msg = changeKeyNickBeanRsp.getMsg();
                        Timber.e("code: %1s, msg: %2s", changeKeyNickBeanRsp.getCode(), msg);
                        if (!code.equals("200")) {
                            if (code.equals("444")) {
                                App.getInstance().logout(true, MapActivity.this);
                                return;
                            }

                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                            }
                            return;
                        } else {
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                            }
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MapActivity.this.finish();
                                }
                            }, 1000);

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);
                        dismissLoading();

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void addGeoFence(LatLng latLng, float radius) {
        Timber.d("addGeoFence: started");
        if (null != App.getInstance().getLockGeoFenceService()) {
            mBleDeviceLocal.setOpenElectricFence(true);
            mBleDeviceLocal.setLatitude(latLng.latitude);
            mBleDeviceLocal.setLongitude(latLng.longitude);
            if (App.getInstance().getLockGeoFenceService().updateDeviceGeo(mBleDeviceLocal, latLng, radius)) {
                // ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.geofence_added_successfully);
                AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
                App.getInstance().getLockAppService().updateDeviceGeoState(mBleDeviceLocal.getMac(), mBleDeviceLocal);
                pushService();
            } else {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.failed_to_add_geofence);
            }
        } else {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.failed_to_add_geofence);
        }
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
