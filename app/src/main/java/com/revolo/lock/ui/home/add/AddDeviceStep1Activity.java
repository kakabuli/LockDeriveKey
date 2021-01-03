package com.revolo.lock.ui.home.add;

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

    private final int RC_CAMERA_PERMISSIONS = 9999;
    private final int RC_READ_EXTERNAL_STORAGE_PERMISSIONS = 8888;

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
            requestCodeReadStorage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions,
                                           int @NotNull [] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void requestCodeCameraPermissions() {
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "TODO: Camera things",
                    RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
        } else {
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE_PERMISSIONS)
    private void requestCodeReadStorage() {
        if(!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "扫描二维码需要存储权限",
                    RC_CAMERA_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE);
        }  else {
            requestCodeCameraPermissions();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Timber.d("requestCode: %1d",requestCode);
        if(perms.get(0).equals(Manifest.permission.CAMERA)) {
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.e("拒绝了扫描二维码需要的储存权限, requestCode: %1d", requestCode);
        } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
            Timber.e("拒绝了扫描二维码需要的相机权限, requestCode: %1d", requestCode);
        }

    }
}
