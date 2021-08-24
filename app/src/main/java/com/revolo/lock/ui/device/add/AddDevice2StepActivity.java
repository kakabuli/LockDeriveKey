package com.revolo.lock.ui.device.add;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.util.LockEasyPermissions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * author : yi
 * time   : 20210817
 * E-mail :
 * desc   : 添加设备第二步
 */
public class AddDevice2StepActivity  extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_QR_CODE_PERMISSIONS = 9999;
    private static final int RC_CAMERA_PERMISSIONS = 7777;
    private static final int RC_READ_EXTERNAL_STORAGE_PERMISSIONS = 8888;
    private VideoView mVideoView;
    private ImageView mHintImageView;
    private ImageView mPlayStateView;
    private int mPlayState = -1;//0正常播放、1、暂停、-1、初始化
    private int mCurrTime = 0;
    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_step2_view;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.add_device_activity_title_2));
        mVideoView = findViewById(R.id.imageView_voice);
        mHintImageView = findViewById(R.id.imageView);
        mPlayStateView = findViewById(R.id.imageview_play);
        applyDebouncingClickListener(findViewById(R.id.btnNext), mVideoView, mHintImageView, mPlayStateView);
        initVoieo();
    }

    @Override
    public void doBusiness() {

    }
    private void initVoieo() {
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //结束
                if (null != mp) {
                    mp.release();
                }
                clearVoieo();
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onPrepared(MediaPlayer mp) {
                //准备好后
                if (mCurrTime != 0) {
                    mVideoView.seekTo(mCurrTime);
                }
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (null != mp) {
                    mp.release();
                }
                clearVoieo();
                return false;
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearVoieo();
    }

    private void clearVoieo() {
        if (null != mVideoView) {
            mVideoView.setVisibility(View.GONE);
            mVideoView.stopPlayback();
            mVideoView.suspend();
        }
        mPlayState = -1;
        mCurrTime = 0;
        mHintImageView.setVisibility(View.VISIBLE);
    }

    private void startPlay() {
        mPlayState = 0;
        mPlayStateView.setVisibility(View.GONE);
        mHintImageView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.smart_voice_2);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
    }

    private void rePlay() {
        mPlayState = 0;
        mPlayStateView.setVisibility(View.GONE);
        Timber.e("dsagag:" + mCurrTime);
        mVideoView.start();
    }

    private void pausePlay() {
        mPlayState = 1;
        mPlayStateView.setVisibility(View.VISIBLE);
        mCurrTime = mVideoView.getCurrentPosition();
        Timber.e("dsagag:" + mCurrTime);
        mVideoView.pause();
    }

    private void onClickPa() {
        if (mPlayState == -1) {
            //播放
            startPlay();
        } else if (mPlayState == 0) {
            //暂停
            pausePlay();
        } else {
            rePlay();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnNext) {
            //rcQRCodePermissions();
            if (!hasCameraPermission()) {
                LockEasyPermissions.requestPermissions(this, getString(R.string.tip_scan_qr_code_need_camera_permission),
                        RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
            } else {
                clearVoieo();
                startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
            }
        }else if (view.getId() == mVideoView.getId()) {
            //播放控件
            onClickPa();
        } else if (view.getId() == mHintImageView.getId()) {
            //播放背景
            onClickPa();
        } else if (view.getId() == mPlayStateView.getId()) {
            //播放按键
            onClickPa();
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
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions,
                                           int @NotNull [] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_QR_CODE_PERMISSIONS)
    private void rcQRCodePermissions() {
        String[] perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.tip_get_read_external_n_camera_permissions),
                    RC_QR_CODE_PERMISSIONS, perms);
        } else {
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void rcCameraPermission() {
        if (!hasCameraPermission()) {
            EasyPermissions.requestPermissions(this, getString(R.string.tip_scan_qr_code_need_camera_permission),
                    RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE_PERMISSIONS)
    private void rcReadStoragePermission() {
        if (!hasReadExternalStoragePermission()) {
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
        if (perms.isEmpty()) {
            Timber.e("onPermissionsGranted 返回的权限不存在数据 perms size: %1d", perms.size());
            return;
        }
        if (requestCode == RC_QR_CODE_PERMISSIONS) {
            if (perms.size() == 2) {
                Timber.d("onPermissionsGranted 同时两条权限都请求成功");
                startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
            } else if (perms.get(0).equals(Manifest.permission.CAMERA)) {
                Timber.d("onPermissionsGranted 只有相机权限成功");
                if (hasReadExternalStoragePermission()) {
                    startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
                } else {
                    rcReadStoragePermission();
                }
            } else if (perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Timber.d("onPermissionsGranted 只有存储权限成功");
                if (hasCameraPermission()) {
                    startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
                } else {
                    rcCameraPermission();
                }
            }
        } else if (requestCode == RC_CAMERA_PERMISSIONS || requestCode == RC_READ_EXTERNAL_STORAGE_PERMISSIONS) {
            Timber.d("onPermissionsGranted 请求剩下的权限成功");
            startActivity(new Intent(this, AddDeviceQRCodeStep2Activity.class));
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.e("onPermissionsDenied 拒绝了扫描二维码需要的储存权限, requestCode: %1d", requestCode);
        } else if (perms.get(0).equals(Manifest.permission.CAMERA)) {
            Timber.e("onPermissionsDenied 拒绝了扫描二维码需要的相机权限, requestCode: %1d", requestCode);
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.pwd_open_camera_permission);
        }

    }
}
