package com.revolo.lock.ui.device.add;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/29
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加设备第一步
 */
public class AddDeviceStep1Activity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_QR_CODE_PERMISSIONS = 9999;
    private static final int RC_CAMERA_PERMISSIONS = 7777;
    private static final int RC_READ_EXTERNAL_STORAGE_PERMISSIONS = 8888;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_step1;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device));
        applyDebouncingClickListener(findViewById(R.id.btnNext));
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnNext) {
            rcQRCodePermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions,
                                           int @NotNull [] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_QR_CODE_PERMISSIONS)
    private void rcQRCodePermissions() {
        String[] perms = new String[] {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.tip_get_read_external_n_camera_permissions),
                    RC_QR_CODE_PERMISSIONS, perms);
        } else {
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void rcCameraPermission() {
        if(!hasCameraPermission()) {
            EasyPermissions.requestPermissions(this, getString(R.string.tip_scan_qr_code_need_camera_permission),
                    RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE_PERMISSIONS)
    private void rcReadStoragePermission(){
        if(!hasReadExternalStoragePermission()) {
            EasyPermissions.requestPermissions(this, getString(R.string.tip_scan_qr_code_need_read_external_permission),
                    RC_READ_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA);
    }

    private boolean hasReadExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(perms.isEmpty()) {
            Timber.e("onPermissionsGranted 返回的权限不存在数据 perms size: %1d", perms.size());
            return;
        }
        if(requestCode == RC_QR_CODE_PERMISSIONS) {
            if(perms.size() == 2) {
                Timber.d("onPermissionsGranted 同时两条权限都请求成功");
                startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
            } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
                Timber.d("onPermissionsGranted 只有相机权限成功");
                if(hasReadExternalStoragePermission()) {
                    startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
                } else {
                    rcReadStoragePermission();
                }
            } else if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Timber.d("onPermissionsGranted 只有存储权限成功");
                if(hasCameraPermission()) {
                    startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
                } else {
                    rcCameraPermission();
                }
            }
        } else if(requestCode == RC_CAMERA_PERMISSIONS || requestCode == RC_READ_EXTERNAL_STORAGE_PERMISSIONS) {
            Timber.d("onPermissionsGranted 请求剩下的权限成功");
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.e("onPermissionsDenied 拒绝了扫描二维码需要的储存权限, requestCode: %1d", requestCode);
        } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
            Timber.e("onPermissionsDenied 拒绝了扫描二维码需要的相机权限, requestCode: %1d", requestCode);
        }

    }
}
