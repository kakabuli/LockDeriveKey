package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DelInvalidShareBeanReq;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.EnableSharedUserBeanReq;
import com.revolo.lock.bean.request.UpdateUserAuthorityTypeBeanReq;
import com.revolo.lock.bean.respone.DelInvalidShareBeanRsp;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.EnableSharedUserBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserAuthorityTypeBeanRsp;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 已分享的用户详情
 */
public class SharedUserDetailActivity extends BaseActivity {

    private ImageView ivEnable, ivFamily, ivGuest, ivUser;
    private String mPreA;
    private GetAllSharedUserFromLockBeanRsp.DataBean mSharedUserData;
    private TextView mTvEsn, mTvUserName;
    private GetAllSharedUserFromAdminUserBeanRsp.DataBean mShareUser;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.PRE_A)) {
            mPreA = intent.getStringExtra(Constant.PRE_A);
        }
        if (intent.hasExtra(Constant.SHARE_USER_DEVICE_DATA)) {
            mSharedUserData = intent.getParcelableExtra(Constant.SHARE_USER_DEVICE_DATA);
        }
        if (intent.hasExtra(Constant.SHARE_USER_DATA)) {
            mShareUser = intent.getParcelableExtra(Constant.SHARE_USER_DATA);
        }
        if (mSharedUserData == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_shared_user_detail;
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
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_management));
        initLoading(getString(R.string.t_load_content_loading));
        ivEnable = findViewById(R.id.ivEnable);
        ivFamily = findViewById(R.id.ivFamily);
        ivGuest = findViewById(R.id.ivGuest);
        ivUser = findViewById(R.id.ivUser);
        mTvEsn = findViewById(R.id.tvEsn);
        mTvUserName = findViewById(R.id.tvUserName);
        ConstraintLayout clFamily = findViewById(R.id.clFamily);
        ConstraintLayout clGuest = findViewById(R.id.clGuest);
        Button btnDelete = findViewById(R.id.btnDelete);

        clFamily.setOnClickListener(v -> {
            if (mSharedUserData.getShareUserType() != 1) {
                switchUserAuthority(true);
            }
        });

        clGuest.setOnClickListener(v -> {
            if (mSharedUserData.getShareUserType() != 2) {
                switchUserAuthority(false);
            }
        });

        btnDelete.setOnClickListener(v -> {
            showRemoveUserDialog();
        });

        ivEnable.setOnClickListener(v -> {
            switchUserEnable(mSharedUserData.getIsEnable() == 1);
        });
    }

    @Override
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btnDelete) {
            showRemoveUserDialog();
        }
    }

    private void refreshUI() {
        // 1 启用 0 未启用
        if (mSharedUserData.getIsEnable() == 1) {
            ivEnable.setImageResource(R.drawable.ic_icon_switch_open);
        } else {
            ivEnable.setImageResource(R.drawable.ic_icon_switch_close);
        }
        // 1 family； 2 guest
        if (mSharedUserData.getShareUserType() == 1) {
            ivFamily.setImageResource(R.drawable.ic_home_password_icon_selected);
            ivGuest.setImageResource(R.drawable.ic_home_password_icon_default);
        } else {
            ivFamily.setImageResource(R.drawable.ic_home_password_icon_default);
            ivGuest.setImageResource(R.drawable.ic_home_password_icon_selected);
        }

        String name;
        String avatar;
        if (mPreA.equals(Constant.USER_MANAGEMENT_A)) {
            name = mSharedUserData.getRemarkName();
            avatar = mSharedUserData.getAvatarPath();
        } else {
            name = mShareUser.getNickName();
            avatar = mShareUser.getAvatarPath();
        }
        mTvUserName.setText(TextUtils.isEmpty(name) ? "" : name);
        mTvEsn.setText("Access to lock device, \n" + (TextUtils.isEmpty(mSharedUserData.getLockNickname()) ? "" : mSharedUserData.getLockNickname()) + " user rights");

        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        //缓存
                .skipMemoryCache(false)
                .error(R.drawable.home_user_authorization_user);
        Glide.with(this)
                .load(TextUtils.isEmpty(avatar) ? "" : avatar)
                .apply(requestOptions)
                .into(ivUser);
    }

    private void showRemoveUserDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_are_you_sure_to_remove_this_user));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            removeUser();
        });
        dialog.show();
    }

    private void removeUser() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("removeUser App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("removeUser token is empty");
            return;
        }
        DelInvalidShareBeanReq req = new DelInvalidShareBeanReq();
        req.setShareId(mSharedUserData.getShareId());
        showLoading();
        Observable<DelInvalidShareBeanRsp> observable = HttpRequest.getInstance().delInvalidShare(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelInvalidShareBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelInvalidShareBeanRsp delInvalidShareBeanRsp) {
                dismissLoading();
                String code = delInvalidShareBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("removeUser code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = delInvalidShareBeanRsp.getMsg();
                    Timber.e("removeUser code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_delete_share_user_suc);
                finish();
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

    private void showIsEnableUser(boolean isEnable) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.setMessage(getString(isEnable
                ? R.string.dialog_tip_operation_authorization_of_revolo_device_is_eabled
                : R.string.dialog_tip_operation_authorization_of_revolo_device_is_disabled));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnListener(v -> dialog.cancel());
        dialog.show();
    }

    private void showChangeUserAuthority(boolean isFamily) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.setMessage(getString(isFamily
                ? R.string.dialog_tip_you_are_authorized_to_be_the_family_user_of_the_revolo_device
                : R.string.dialog_tip_you_are_authorized_to_be_the_guest_user_of_the_revolo_device));
        dialog.setOnListener(v -> dialog.cancel());
        dialog.show();
    }

    private void switchUserEnable(boolean isEnable) {
        if (!checkNetConnectFail()) {
            return;
        }
        if (mSharedUserData == null) {
            Timber.e("switchUserEnable mSharedUserData == null");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("switchUserEnable App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("switchUserEnable token is empty");
            return;
        }
        EnableSharedUserBeanReq req = new EnableSharedUserBeanReq();
        req.setIsEnable(isEnable ? 0 : 1);
        req.setShareId(mSharedUserData.getShareId());
        req.setUid(App.getInstance().getUserBean().getUid());
        showLoading();
        Observable<EnableSharedUserBeanRsp> observable = HttpRequest.getInstance().enableSharedUser(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<EnableSharedUserBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull EnableSharedUserBeanRsp enableSharedUserBeanRsp) {
                dismissLoading();
                String code = enableSharedUserBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("switchUserEnable code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = enableSharedUserBeanRsp.getMsg();
                    Timber.e("switchUserEnable code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                mSharedUserData.setIsEnable(isEnable ? 0 : 1);
                refreshUI();
                showIsEnableUser(isEnable);
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

    private void switchUserAuthority(boolean isFamily) {
        if (!checkNetConnectFail()) {
            return;
        }
        if (mSharedUserData == null) {
            Timber.e("switchUserAuthority mSharedUserData == null");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("switchUserAuthority App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("switchUserAuthority token is empty");
            return;
        }
        UpdateUserAuthorityTypeBeanReq req = new UpdateUserAuthorityTypeBeanReq();
        req.setShareId(mSharedUserData.getShareId());
        req.setShareUserType(isFamily ? 1 : 2);
        req.setUid(App.getInstance().getUserBean().getUid());
        showLoading();
        Observable<UpdateUserAuthorityTypeBeanRsp> observable = HttpRequest.getInstance().updateUserAuthorityType(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<UpdateUserAuthorityTypeBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull UpdateUserAuthorityTypeBeanRsp updateUserAuthorityTypeBeanRsp) {
                dismissLoading();
                String code = updateUserAuthorityTypeBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("switchUserAuthority code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = updateUserAuthorityTypeBeanRsp.getMsg();
                    Timber.e("switchUserAuthority code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                mSharedUserData.setShareUserType(isFamily ? 1 : 2);
                refreshUI();
                showChangeUserAuthority(isFamily);
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
