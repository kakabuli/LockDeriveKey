package com.revolo.lock.ui.device.lock.setting.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.dialog.PrivacyPolicyDialog;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.device.lock.setting.GeoFenceUnlockActivity;

import timber.log.Timber;

/**
 * author : zhougm
 * time   : 2021/8/3
 * E-mail : zhouguimin@kaadas.com
 * desc   :
 */
public class AutoUnlockActivity extends BaseActivity {

    private BleDeviceLocal mBleDeviceLocal;
    private ImageView mIvGeoFenceUnlockEnable;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auto_unlock;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_geo_fence_unlock));
        mIvGeoFenceUnlockEnable = findViewById(R.id.ivGeoFenceUnlockEnable);
        mIvGeoFenceUnlockEnable.setOnClickListener(v -> {
            checkLocation();
        });
    }

    public void checkLocation() {
        if (ContextCompat.checkSelfPermission(App.getInstance().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Timber.e("Location定位权限开启");
            Intent intent = new Intent(this, GeoFenceUnlockActivity.class);
            startActivity(intent);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show a dialog for displaying why the permission is needed and the ask the permission
                Timber.e("Location定位权限拒绝");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.dialog_we_need_to_permission_for_location));
            } else {
                Timber.e("Location定位权限开启开启中");
                onPrivacyPolicyDialog();
            }
        }
    }

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;

    private PrivacyPolicyDialog privacyPolicyDialog;

    private void onPrivacyPolicyDialog() {
        if (privacyPolicyDialog == null) {
            privacyPolicyDialog = new PrivacyPolicyDialog(this);
        } else {
            privacyPolicyDialog.dismiss();
        }
        privacyPolicyDialog.setOnConfirmListener(v -> {
            privacyPolicyDialog.dismiss();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        });
        privacyPolicyDialog.setOnCancelClickListener(v -> {
            privacyPolicyDialog.dismiss();
            finish();
        });
        privacyPolicyDialog.show();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBleDeviceLocal != null) {
            mIvGeoFenceUnlockEnable.setImageResource(mBleDeviceLocal.isOpenElectricFence() ? R.drawable.ic_icon_switch_open : R.drawable.ic_icon_switch_close);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, GeoFenceUnlockActivity.class);
                startActivity(intent);
            }
        }
    }
}
