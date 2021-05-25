package com.revolo.lock.ui.sign;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.UploadUserAvatarBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.util.GlideEngine;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class RegisterAddAvatarActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private User mUser;
    private static final int RC_QR_CODE_PERMISSIONS = 9999;
    private static final int RC_CAMERA_PERMISSIONS = 7777;
    private static final int RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 9999;
    private ImageView mIvAvatar;

    private boolean isCanUploadAvatar = false;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register_add_avatar;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
        applyDebouncingClickListener(findViewById(R.id.tvSkip),
                findViewById(R.id.btnAlbumAdd),
                findViewById(R.id.btnPhotograph));
        mUser = App.getInstance().getUser();
        mIvAvatar = findViewById(R.id.ivAvatar);
        rcSelectPicPermissions();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.tvSkip) {
            startActivity(new Intent(this, RegisterAddAvatarNextActivity.class));
            return;
        }
        if(view.getId() == R.id.btnPhotograph) {
            rcSelectPicPermissions();
            if(!isCanUploadAvatar) {
                return;
            }
            PictureSelector.create(this)
                    .openCamera(PictureMimeType.ofImage())
                    .maxSelectNum(1)
                    .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .forResult(PictureConfig.REQUEST_CAMERA);
            return;
        }
        if(view.getId() == R.id.btnAlbumAdd) {
            rcSelectPicPermissions();
            if(!isCanUploadAvatar) {
                return;
            }
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .forResult(PictureConfig.CHOOSE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                case PictureConfig.REQUEST_CAMERA:
                    // 结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    if(selectList == null) {
                        return;
                    }
                    if(selectList.isEmpty()) {
                        return;
                    }
                    String path = selectList.get(0).getRealPath();
                    if(TextUtils.isEmpty(path)) {
                        return;
                    }
                    File avatarFile = new File(path);
                    mUser.setAvatarLocalPath(path);
                    uploadUserAvatar(avatarFile);

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions,
                                           int @NotNull [] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
                isCanUploadAvatar = true;
            } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
                Timber.d("onPermissionsGranted 只有相机权限成功");
                if(hasWriteExternalStoragePermission()) {
                    isCanUploadAvatar = true;
                } else {
                    rcWriteStoragePermission();
                }
            } else if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Timber.d("onPermissionsGranted 只有存储权限成功");
                if(hasCameraPermission()) {
                    isCanUploadAvatar = true;
                } else {
                    rcCameraPermission();
                }
            }
        } else if(requestCode == RC_CAMERA_PERMISSIONS || requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS) {
            Timber.d("onPermissionsGranted 请求剩下的权限成功");
            isCanUploadAvatar = true;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.e("onPermissionsDenied 拒绝了打开图库需要的储存权限, requestCode: %1d", requestCode);
        } else if(perms.get(0).equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Timber.e("onPermissionsDenied 拒绝了打开图库需要的写入权限, requestCode: %1d", requestCode);
        } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
            Timber.e("onPermissionsDenied 拒绝了打开相机需要的相机权限, requestCode: %1d", requestCode);
        }
    }

    private void refreshAvatar() {
        String avatarUrl = mUser.getAvatarUrl();
        String avatarLocalPath = mUser.getAvatarLocalPath();
        String url;
        if(TextUtils.isEmpty(avatarLocalPath)) {
            url = avatarUrl;
        } else {
            File file = new File(avatarLocalPath);
            if(file.exists()) {
                url = avatarLocalPath;
            } else {
                url = avatarUrl;
            }
        }
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)        //不做磁盘缓存
                .skipMemoryCache(true)                            //不做内存缓存
                .error(R.drawable.default_avatar)          //错误图片
                .placeholder(R.drawable.default_avatar);   //预加载图片
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_avatar)
                .apply(requestOptions)
                .into(mIvAvatar);
    }

    private void uploadUserAvatar(@NotNull File avatarFile) {
        if(!checkNetConnectFail()) {
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("uploadUserAvatar App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("uploadUserAvatar token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("uploadUserAvatar uid is empty");
            return;
        }

        showLoading("Uploading...");
        Observable<UploadUserAvatarBeanRsp> observable = HttpRequest.getInstance()
                .uploadUserAvatar(token, uid, avatarFile);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UploadUserAvatarBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull UploadUserAvatarBeanRsp uploadUserAvatarBeanRsp) {
                dismissLoading();
                String code = uploadUserAvatarBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("uploadUserAvatar code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    String msg = uploadUserAvatarBeanRsp.getMsg();
                    Timber.e("uploadUserAvatar code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                String avatarUrl = uploadUserAvatarBeanRsp.getData().getPath();
                if(TextUtils.isEmpty(avatarUrl)) {
                    Timber.e("avatarUrl is empty");
                    return;
                }
                refreshAvatar();
                mUser.setAvatarUrl(avatarUrl);
                AppDatabase.getInstance(RegisterAddAvatarActivity.this).userDao().update(mUser);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(RegisterAddAvatarActivity.this, RegisterAddAvatarNextActivity.class));
                    finish();
                }, 50);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @AfterPermissionGranted(RC_QR_CODE_PERMISSIONS)
    private void rcSelectPicPermissions() {
        String[] perms = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rq_use_fun_need_camera_n_write_permission),
                    RC_QR_CODE_PERMISSIONS, perms);
        } else {
            isCanUploadAvatar = true;
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void rcCameraPermission() {
        if(!hasCameraPermission()) {
            EasyPermissions.requestPermissions(this, getString(R.string.rq_use_the_camera_needs_camera_permission),
                    RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS)
    private void rcWriteStoragePermission(){
        if(!hasWriteExternalStoragePermission()) {
            EasyPermissions.requestPermissions(this, getString(R.string.rq_use_album_needs_write_permission),
                    RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA);
    }

    private boolean hasWriteExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

}
