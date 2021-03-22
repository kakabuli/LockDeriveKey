package com.revolo.lock.ui.mine;

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
import com.donkingliang.imageselector.utils.ImageSelector;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.popup.PicSelectPopup;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户页面
 */
public class UserPageActivity extends BaseActivity {

    private User mUser;
    private ImageView mIvAvatar;
    private PicSelectPopup mPicSelectPopup;

    private final int RC_CAMERA_PERMISSIONS = 7777;
    private final int RC_READ_EXTERNAL_STORAGE_PERMISSIONS = 8888;
    private final int RC_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 9999;

    private final int REQUEST_CODE_TAKE_PIC = 1111;
    private final int REQUEST_CODE_SELECT_PIC = 2222;

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
        initLoading("Logging out...");
        mPicSelectPopup = new PicSelectPopup(this);
        mPicSelectPopup.setPicSelectOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(true)  //设置是否单选
                        .canPreview(true) //是否可以预览图片，默认为true
                        .start(UserPageActivity.this, REQUEST_CODE_SELECT_PIC); // 打开相册
            }
        });
        mPicSelectPopup.setCameraOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageSelector.builder()
                        .onlyTakePhoto(true)
                        .start(UserPageActivity.this, REQUEST_CODE_TAKE_PIC);
            }
        });
        mPicSelectPopup.setCancelOnClickListener(v -> {
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
                String avatarUrl = mUser.getAvatarUrl();
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.mine_personal_img_headportrait_default)
                        .into(mIvAvatar);
            }
        });
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
            if(mPicSelectPopup != null) {
                mPicSelectPopup.setPopupGravity(Gravity.BOTTOM);
                mPicSelectPopup.showPopupWindow();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PIC && data != null) {
            //获取选择器返回的数据
            ArrayList<String> images = data.getStringArrayListExtra(
                    ImageSelector.SELECT_RESULT);

            /**
             * 是否是来自于相机拍照的图片，
             * 只有本次调用相机拍出来的照片，返回时才为true。
             * 当为true时，图片返回的结果有且只有一张图片。
             */
            boolean isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
        }
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
        showLoading();
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
