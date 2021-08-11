package com.revolo.lock.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDetailDevicesAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.ShareUserDetailBean;
import com.revolo.lock.bean.request.DelInvalidShareBeanReq;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.request.GetDevicesFromUidAndSharedUidBeanReq;
import com.revolo.lock.bean.respone.DelInvalidShareBeanRsp;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.device.lock.SharedUserDetailActivity;
import com.revolo.lock.widget.SlideRecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户详情页面
 */
public class AuthUserDetailActivity extends BaseActivity {

    private static final int RESULT_CODE = 0xf01;
    private AuthUserDetailDevicesAdapter mDevicesAdapter;
    private TextView mTvDeviceNum, mTvAccount, mTvUserName;
    private GetAllSharedUserFromAdminUserBeanRsp.DataBean mShareUser;
    private ImageView ivAvatar;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.SHARE_USER_DATA)) {
            mShareUser = intent.getParcelableExtra(Constant.SHARE_USER_DATA);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auth_user_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_users))
                .setRight(R.drawable.ic_home_icon_add, v -> {
                    Intent intent = new Intent(AuthUserDetailActivity.this, AddDeviceForSharedUserActivity.class);
                    intent.putExtra(Constant.SHARE_USER_MAIL, mShareUser.getUserMail());
                    intent.putExtra(Constant.SHARE_USER_DATA, mShareUser.getShareUid());
                    intent.putExtra(Constant.SHARE_USER_FIRST_NAME, mShareUser.getFirstName());
                    intent.putExtra(Constant.SHARE_USER_LAST_NAME, mShareUser.getLastName());
                    startActivity(intent);
                });
        SlideRecyclerView shareList = findViewById(R.id.shareList);
        ivAvatar = findViewById(R.id.ivAvatar);
        mTvDeviceNum = findViewById(R.id.tvDeviceNum);
        mTvUserName = findViewById(R.id.tvUserName);
        mTvAccount = findViewById(R.id.tvAccount);
        mDevicesAdapter = new AuthUserDetailDevicesAdapter(new ArrayList<>());
        mDevicesAdapter.setOnItemClickListener((adapter, view, position) -> {
            GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean = (GetDevicesFromUidAndSharedUidBeanRsp.DataBean) adapter.getItem(position);
            if (dataBean != null && dataBean.getShareUserType() != -1) {
                ShareUserDetailBean shareUserDetailBean = new ShareUserDetailBean();
                shareUserDetailBean.setAvatar(mShareUser.getAvatarPath());
                shareUserDetailBean.setName(dataBean.getFirstName() + " " + dataBean.getLastName());
                shareUserDetailBean.setIsEnable(dataBean.getIsEnable());
                shareUserDetailBean.setShareId(dataBean.getShareId());
                shareUserDetailBean.setShareUserType(dataBean.getShareUserType());
                Intent intent = new Intent(AuthUserDetailActivity.this, SharedUserDetailActivity.class);
                intent.putExtra(Constant.SHARE_USER_DATA, shareUserDetailBean);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            showRemoveUserDialog();
        });
        mDevicesAdapter.setOnReInviteListener(this::share);
        mDevicesAdapter.setOnDeleteListener(dataBean -> {
            if (dataBean != null) {
                showRemoveDeviceDialog(dataBean);
            }
            shareList.closeMenu();
        });
        shareList.setLayoutManager(new LinearLayoutManager(this));
        shareList.setAdapter(mDevicesAdapter);
        applyDebouncingClickListener(findViewById(R.id.clUserName));
        initLoading(getString(R.string.t_load_content_loading));

    }

    @Override
    public void doBusiness() {
        refreshUI();
        searchUserDevice();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.clUserName) {
            Intent intent = new Intent(this, ChangeSharedUserNameActivity.class);
            intent.putExtra(Constant.SHARE_USER_DATA, mShareUser.getShareUid());
            intent.putExtra(Constant.SHARE_USER_FIRST_NAME, mShareUser.getFirstName());
            intent.putExtra(Constant.SHARE_USER_LAST_NAME, mShareUser.getLastName());
            startActivityForResult(intent, RESULT_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == Activity.RESULT_OK) {
            String firstName = data.getStringExtra(Constant.SHARE_USER_FIRST_NAME);
            String lastName = data.getStringExtra(Constant.SHARE_USER_LAST_NAME);
            mShareUser.setFirstName(TextUtils.isEmpty(firstName) ? "" : firstName);
            mShareUser.setLastName(TextUtils.isEmpty(lastName) ? "" : lastName);
            refreshUI();
        }
    }

    private void refreshUI() {
        mTvAccount.setText(mShareUser.getUserMail());
        mTvUserName.setText(mShareUser.getFirstName() + " " + mShareUser.getLastName());
        mTvDeviceNum.setText(String.valueOf(mShareUser.getDeviceCount()));
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        //缓存
                .skipMemoryCache(false)
                .error(R.drawable.home_user_authorization_user);
        Glide.with(this)
                .load(TextUtils.isEmpty(mShareUser.getAvatarPath()) ? "" : mShareUser.getAvatarPath())
                .apply(requestOptions)
                .into(ivAvatar);
    }

    private void showRemoveDeviceDialog(GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean) {
        if (dataBean.getShareUserType() == -1) {
            return;
        }
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_are_you_sure_to_remove_this_user));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            deleteShare(dataBean);
        });
        dialog.show();
    }

    private void searchUserDevice() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("searchUserDevice App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("searchUserDevice token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("searchUserDevice uid is empty");
            return;
        }

        GetDevicesFromUidAndSharedUidBeanReq req = new GetDevicesFromUidAndSharedUidBeanReq();
        req.setAdminUid(uid);
        req.setShareUId(mShareUser.getShareUid());
        showLoading();
        Observable<GetDevicesFromUidAndSharedUidBeanRsp> observable = HttpRequest.getInstance().getDevicesFromUidAndSharedUid(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetDevicesFromUidAndSharedUidBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetDevicesFromUidAndSharedUidBeanRsp getDevicesFromUidAndSharedUidBeanRsp) {
                dismissLoading();
                String code = getDevicesFromUidAndSharedUidBeanRsp.getCode();
                String msg = getDevicesFromUidAndSharedUidBeanRsp.getMsg();
                if (code.equals("200")) {
                    List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> dataBeans = getDevicesFromUidAndSharedUidBeanRsp.getData();
                    if (dataBeans != null && !dataBeans.isEmpty()) {
                        groupData(dataBeans);
                    } else {
                        finish();
                    }
                } else if (code.equals("444")) {
                    App.getInstance().logout(true, AuthUserDetailActivity.this);
                } else {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                }
                refreshUI();
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

    private void groupData(@NonNull List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> dataBeans) {

        List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> families = new ArrayList<>();
        List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> guests = new ArrayList<>();
        List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> shareUsers = new ArrayList<>();
        for (int i = 0; i < dataBeans.size(); i++) {
            GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean = dataBeans.get(i);
            if (dataBean.getShareUserType() == 1) {
                families.add(dataBean);
            } else if (dataBean.getShareUserType() == 2) {
                guests.add(dataBean);
            }
        }
        if (families.size() > 0) {
            GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean = new GetDevicesFromUidAndSharedUidBeanRsp.DataBean();
            dataBean.setShareUserType(-1);
            dataBean.setLockNickname("family");
            shareUsers.add(dataBean);
            shareUsers.addAll(families);
        }

        if (guests.size() > 0) {
            GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean = new GetDevicesFromUidAndSharedUidBeanRsp.DataBean();
            dataBean.setShareUserType(-1);
            dataBean.setLockNickname("guest");
            shareUsers.add(dataBean);
            shareUsers.addAll(guests);
        }
        mDevicesAdapter.setList(shareUsers);
    }

    private void share(@NonNull GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean) {
        if (!dataBean.getShareState().equals("3") || dataBean.getShareUserType() == -1) { // 非等待状态不能分享
            return;
        }
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("share App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("share uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("share token is empty");
            return;
        }
        GainKeyBeanReq req = new GainKeyBeanReq();
        req.setDeviceSN(dataBean.getDeviceSN());
        req.setShareUserType(dataBean.getShareUserType());
        req.setAdminUid(uid);
        req.setFirstName(dataBean.getFirstName());
        req.setLastName(dataBean.getLastName());
        req.setShareAccount(mShareUser.getUserMail());
        showLoading();
        Observable<GainKeyBeanRsp> observable = HttpRequest.getInstance().gainKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GainKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GainKeyBeanRsp gainKeyBeanRsp) {
                dismissLoading();
                String code = gainKeyBeanRsp.getCode();
                String msg = gainKeyBeanRsp.getMsg();
                if (code.equals("200")) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.tip_content_share_successful));
                    searchUserDevice();
                } else if (code.equals("444")) {
                    App.getInstance().logout(true, AuthUserDetailActivity.this);
                } else {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                }
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
        DelSharedUserBeanReq req = new DelSharedUserBeanReq();
        req.setShareUid(mShareUser.getShareUid());
        req.setUid(App.getInstance().getUserBean().getUid());
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
                if (TextUtils.isEmpty(code)) {
                    Timber.e("removeUser code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, AuthUserDetailActivity.this);
                        return;
                    }
                    String msg = delSharedUserBeanRsp.getMsg();
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

    private void deleteShare(GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean) {
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
        req.setShareId(dataBean.getShareId());
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
                        App.getInstance().logout(true, AuthUserDetailActivity.this);
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
}
