package com.revolo.lock.ui.mine;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.bean.respone.UploadUserAvatarBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.popup.PicSelectPopup;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;
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
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户页面
 */
public class UserPageActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private User mUser;
    private ImageView mIvAvatar;
    private PicSelectPopup mPicSelectPopup;

    private final int RC_QR_CODE_PERMISSIONS = 9999;
    private final int RC_CAMERA_PERMISSIONS = 7777;
    private final int RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 9999;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_user_page;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_user_page));
        mIvAvatar = findViewById(R.id.ivAvatar);
        applyDebouncingClickListener(findViewById(R.id.clUserName), findViewById(R.id.clChangePwd), findViewById(R.id.btnLogout), mIvAvatar);
        mUser = App.getInstance().getUser();
        mPicSelectPopup = new PicSelectPopup(this);
        mPicSelectPopup.setPicSelectOnClickListener(v -> {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .forResult(PictureConfig.CHOOSE_REQUEST);
        });
        mPicSelectPopup.setCameraOnClickListener(v ->
                PictureSelector.create(this)
                        .openCamera(PictureMimeType.ofImage())
                        .maxSelectNum(1)
                        .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                        .forResult(PictureConfig.REQUEST_CAMERA)
        );
        mPicSelectPopup.setCancelOnClickListener(v -> dismissPicSelect());
    }

    private void dismissPicSelect() {
        runOnUiThread(() -> {
            if(mPicSelectPopup != null) {
                mPicSelectPopup.dismiss();
            }
        });
    }

    private void refreshUserUI() {
        runOnUiThread(() -> {
            if(mUser != null) {
                TextView tvUserName = findViewById(R.id.tvUserName);
                TextView tvEmailAddress = findViewById(R.id.tvEmailAddress);
                String userName = mUser.getFirstName();
                // TODO: 2021/3/7 名字后面需要更改其他显示
                tvUserName.setText(TextUtils.isEmpty(userName)?"":userName);
                String email = mUser.getMail();
                tvEmailAddress.setText(TextUtils.isEmpty(email)?"":email);
                refreshAvatar();
            }
        });
    }

    private void refreshAvatar() {
        String avatarUrl = mUser.getAvatarUrl();
        String avatarLocalPath = mUser.getAvatarLocalPath();
        String url;
        if(TextUtils.isEmpty(avatarLocalPath)) {
            url = avatarUrl;
        } else {
            File file = new File(avatarLocalPath);
            if(file == null) {
                url = avatarUrl;
            } else {
                if(file.exists()) {
                    url = avatarLocalPath;
                } else {
                    url = avatarUrl;
                }
            }
        }
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)        //不做磁盘缓存
                .skipMemoryCache(true)                            //不做内存缓存
                .error(R.drawable.mine_personal_img_headportrait_default)          //错误图片
                .placeholder(R.drawable.mine_personal_img_headportrait_default);   //预加载图片
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.mine_personal_img_headportrait_default)
                .apply(requestOptions)
                .into(mIvAvatar);
    }

    @Override
    public void doBusiness() {
        refreshUserUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clUserName) {
            Intent intent = new Intent(this, ModifyUserNameActivity.class);
            startActivity(intent);
            return;
        }
        if(view.getId() == R.id.clChangePwd) {
            startActivity(new Intent(this, ModifyPasswordActivity.class));
            return;
        }
        if(view.getId() == R.id.btnLogout) {
            showLogoutDialog();
            return;
        }
        if(view.getId() == R.id.ivAvatar) {
            rcSelectPicPermissions();
        }
    }

    private void showSelectPopup() {
        if(mPicSelectPopup != null) {
            mPicSelectPopup.setPopupGravity(Gravity.BOTTOM);
            mPicSelectPopup.showPopupWindow();
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
                    if(avatarFile == null) {
                        return;
                    }
                    mUser.setAvatarLocalPath(path);
                    uploadUserAvatar(avatarFile);
                    dismissPicSelect();

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
                showSelectPopup();
            } else if(perms.get(0).equals(Manifest.permission.CAMERA)) {
                Timber.d("onPermissionsGranted 只有相机权限成功");
                if(hasWriteExternalStoragePermission()) {
                    showSelectPopup();
                } else {
                    rcWriteStoragePermission();
                }
            } else if(perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Timber.d("onPermissionsGranted 只有存储权限成功");
                if(hasCameraPermission()) {
                    showSelectPopup();
                } else {
                    rcCameraPermission();
                }
            }
        } else if(requestCode == RC_CAMERA_PERMISSIONS || requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS) {
            Timber.d("onPermissionsGranted 请求剩下的权限成功");
            showSelectPopup();
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

    private void uploadUserAvatar(@NotNull File avatarFile) {

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
                mUser.setAvatarUrl(avatarUrl);
                AppDatabase.getInstance(UserPageActivity.this).userDao().update(mUser);
                refreshUserUI();
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
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "If you want to use the function needs camera permission and write storage permission",
                    RC_QR_CODE_PERMISSIONS, perms);
        } else {
            showSelectPopup();
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void rcCameraPermission() {
        if(!hasCameraPermission()) {
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "If you want to use the camera needs camera permission",
                    RC_CAMERA_PERMISSIONS, Manifest.permission.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS)
    private void rcWriteStoragePermission(){
        if(!hasWriteExternalStoragePermission()) {
            // TODO: 2021/1/3 use string
            EasyPermissions.requestPermissions(this, "If you want to use photo album needs write storage permission",
                    RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA);
    }

    private boolean hasWriteExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void showLogoutDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_log_out));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            logout();
        });
        dialog.show();
    }

    private void logout() {
        if(App.getInstance().getUserBean() == null) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            return;
        }
        showLoading("Logging out...");
        Observable<LogoutBeanRsp> observable = HttpRequest.getInstance().logout(token);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LogoutBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LogoutBeanRsp logoutBeanRsp) {
                dismissLoading();
                String code = logoutBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    String msg = logoutBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                        return;
                    }
                }
                if(App.getInstance().getMainActivity() != null) {
                    App.getInstance().getMainActivity().finish();
                }
                finish();
                startActivity(new Intent(UserPageActivity.this, LoginActivity.class));
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

}
