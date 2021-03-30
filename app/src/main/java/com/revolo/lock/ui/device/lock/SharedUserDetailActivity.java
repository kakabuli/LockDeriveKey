package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.EnableSharedUserBeanReq;
import com.revolo.lock.bean.request.UpdateUserAuthorityTypeBeanReq;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.EnableSharedUserBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserAuthorityTypeBeanRsp;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

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

    private ImageView ivEnable, ivFamily, ivGuest;
    private String mPreA;
    private BleDeviceLocal mBleDeviceLocal;
    private GetAllSharedUserFromLockBeanRsp.DataBean mSharedUserData;
    private TextView mTvEsn, mTvUserName;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PRE_A)) {
            mPreA = intent.getStringExtra(Constant.PRE_A);
        }
        if(TextUtils.isEmpty(mPreA)) {
            finish();
            return;
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        // TODO: 2021/3/8 处理
        if(mBleDeviceLocal == null) {
            finish();
            return;
        }
        if(intent.hasExtra(Constant.SHARED_USER_DATA)) {
            mSharedUserData = intent.getParcelableExtra(Constant.SHARED_USER_DATA);
        }
        if(mSharedUserData == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_shared_user_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_management));
        initLoading("Loading...");
        ivEnable = findViewById(R.id.ivEnable);
        ivFamily = findViewById(R.id.ivFamily);
        ivGuest = findViewById(R.id.ivGuest);
        mTvEsn = findViewById(R.id.tvEsn);
        mTvUserName = findViewById(R.id.tvUserName);
        ConstraintLayout clFamily = findViewById(R.id.clFamily);
        ConstraintLayout clGuest = findViewById(R.id.clGuest);
        Button btnDelete = findViewById(R.id.btnDelete);
        applyDebouncingClickListener(clFamily, clGuest, ivEnable, btnDelete);
    }

    @Override
    public void doBusiness() {
        refreshUI();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clFamily) {
            if(mSharedUserData.getShareUserType() != 1) {
                switchUserAuthority(true);
            }
            return;
        }
        if(view.getId() == R.id.clGuest) {
            if(mSharedUserData.getShareUserType() != 2) {
                switchUserAuthority(false);
            }
            return;
        }
        if(view.getId() == R.id.btnDelete) {
            showRemoveUserDialog();
            return;
        }
        if(view.getId() == R.id.ivEnable) {
            switchUserEnable(mSharedUserData.getIsEnable() != 1);
        }
    }
    
    private void refreshUI() {
        // 1 启用 0 未启用
        if(mSharedUserData.getIsEnable() == 1) {
            ivEnable.setImageResource(R.drawable.ic_icon_switch_open);
        } else {
            ivEnable.setImageResource(R.drawable.ic_icon_switch_close);
        }
        // 1 family； 2 guest
        if(mSharedUserData.getShareUserType() == 1) {
            ivFamily.setImageResource(R.drawable.ic_home_password_icon_selected);
            ivGuest.setImageResource(R.drawable.ic_home_password_icon_default);
        } else {
            ivFamily.setImageResource(R.drawable.ic_home_password_icon_default);
            ivGuest.setImageResource(R.drawable.ic_home_password_icon_selected);
        }
        mTvEsn.setText(mBleDeviceLocal.getEsn());
        String name = mSharedUserData.getUserNickname();
        mTvUserName.setText(TextUtils.isEmpty(name)?"":name);
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
        if(mSharedUserData == null) {
            Timber.e("removeUser mSharedUserData == null");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("removeUser App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("removeUser token is empty");
            return;
        }
        DelSharedUserBeanReq req = new DelSharedUserBeanReq();
        req.setDeviceSN(mBleDeviceLocal.getEsn());
        req.setShareId(mSharedUserData.get_id());
        req.setUid(mSharedUserData.getAdminUid());
        showLoading();
        Observable<DelSharedUserBeanRsp> observable = HttpRequest.getInstance().delSharedUser(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelSharedUserBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelSharedUserBeanRsp delSharedUserBeanRsp) {
                dismissLoading();
                String code = delSharedUserBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("removeUser code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = delSharedUserBeanRsp.getMsg();
                    Timber.e("removeUser code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                // TODO: 2021/3/8 修改提示语 
                ToastUtils.showShort("Delete Share User Success");
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
                ?R.string.dialog_tip_operation_authorization_of_revolo_device_is_eabled
                :R.string.dialog_tip_operation_authorization_of_revolo_device_is_disabled));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnListener(v -> {
            dialog.cancel();
        });
        dialog.show();
    }

    private void showChangeUserAuthority(boolean isFamily) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.setMessage(getString(isFamily
                ?R.string.dialog_tip_you_are_authorized_to_be_the_family_user_of_the_revolo_device
                :R.string.dialog_tip_you_are_authorized_to_be_the_guest_user_of_the_revolo_device));
        dialog.setOnListener(v -> dialog.cancel());
        dialog.show();
    }

    private void switchUserEnable(boolean isEnable) {
        if(mSharedUserData == null) {
            Timber.e("switchUserEnable mSharedUserData == null");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("switchUserEnable App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("switchUserEnable token is empty");
            return;
        }
        EnableSharedUserBeanReq req = new EnableSharedUserBeanReq();
        req.setIsEnable(isEnable?1:0);
        req.setShareId(mSharedUserData.get_id());
        req.setUid(mSharedUserData.getAdminUid());
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
                if(TextUtils.isEmpty(code)) {
                    Timber.e("switchUserEnable code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = enableSharedUserBeanRsp.getMsg();
                    Timber.e("switchUserEnable code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                mSharedUserData.setIsEnable(isEnable?1:0);
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
        if(mSharedUserData == null) {
            Timber.e("switchUserAuthority mSharedUserData == null");
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("switchUserAuthority App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("switchUserAuthority token is empty");
            return;
        }
        UpdateUserAuthorityTypeBeanReq req = new UpdateUserAuthorityTypeBeanReq();
        req.setShareId(mSharedUserData.get_id());
        req.setShareUserType(isFamily?1:2);
        req.setUid(mSharedUserData.getAdminUid());
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
                if(TextUtils.isEmpty(code)) {
                    Timber.e("switchUserAuthority code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, SharedUserDetailActivity.this);
                        return;
                    }
                    String msg = updateUserAuthorityTypeBeanRsp.getMsg();
                    Timber.e("switchUserAuthority code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                mSharedUserData.setShareUserType(isFamily?1:2);
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
