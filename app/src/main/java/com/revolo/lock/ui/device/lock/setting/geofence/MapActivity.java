package com.revolo.lock.ui.device.lock.setting.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;

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
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/3/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private String GEO_FENCE_ID = "SOME_GEO_FENCE_ID";
    private GoogleMap mMap;
    private GeofencingClient mGeoFencingClient;
    private GeoFenceHelper mGeoFenceHelper;
    public float GEO_FENCE_RADIUS = 200;

    private FusedLocationProviderClient fusedLocationClient;

    private BleDeviceLocal mBleDeviceLocal;

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

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setStatusBarColor(R.color.white);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mGeoFencingClient = LocationServices.getGeofencingClient(this);
        mGeoFenceHelper = new GeoFenceHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        if (mMap != null) {
                            LatLng dhaka = new LatLng(location.getLatitude(), location.getLongitude());
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
                });
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
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
        }
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
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show a dialog for displaying why the permission is needed and the ask the permission
                new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_we_need_to_permission_for_location)
                        .setCancelable(true)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                            }
                        }).show();

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
                    new AlertDialog.Builder(MapActivity.this)
                            .setMessage(R.string.dialog_go_to_settings_enable_this_permission)
                            .setPositiveButton(R.string.dialog_go_to_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    gotoApplicationSettings();
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .setCancelable(false)
                            .show();
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
        addGeoFence(latLng, GEO_FENCE_RADIUS);
    }

    private void saveLatLngToLocal(@NotNull LatLng latLng) {
        mBleDeviceLocal.setLatitude(latLng.latitude);
        mBleDeviceLocal.setLongitude(latLng.longitude);
        mBleDeviceLocal.setOpenElectricFence(true);
        AppDatabase.getInstance(this).bleDeviceDao().update(mBleDeviceLocal);
    }

    @SuppressLint("MissingPermission")
    private void addGeoFence(LatLng latLng, float radius) {
        Timber.d("addGeoFence: started");

        Geofence geofence = mGeoFenceHelper.getGeoFence(
                GEO_FENCE_ID,
                latLng,
                radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        GeofencingRequest geofencingRequest = mGeoFenceHelper.getGeoFencingRequest(geofence);

        PendingIntent pendingIntent = mGeoFenceHelper.getPendingIntent();
        mGeoFencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Timber.d("onSuccess: GeoFence Added........"))
                .addOnFailureListener(e -> {
                    String errorMessage = mGeoFenceHelper.getErrorString(e);
                    Timber.d("GeoFence onFailure: %1s", errorMessage);
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

}
