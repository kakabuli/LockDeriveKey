package com.revolo.lock.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDetailDevicesAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.request.GetDevicesFromUidAndSharedUidBeanReq;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.device.lock.SharedUserDetailActivity;

import org.w3c.dom.Text;

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
        RecyclerView rvLockList = findViewById(R.id.rvLockList);
        ivAvatar = findViewById(R.id.ivAvatar);
        mTvDeviceNum = findViewById(R.id.tvDeviceNum);
        mTvUserName = findViewById(R.id.tvUserName);
        mTvAccount = findViewById(R.id.tvAccount);
        mDevicesAdapter = new AuthUserDetailDevicesAdapter(R.layout.item_user_devices_rv);
        mDevicesAdapter.setOnItemClickListener((adapter, view, position) -> {
            GetAllSharedUserFromLockBeanRsp.DataBean dataBean = (GetAllSharedUserFromLockBeanRsp.DataBean) adapter.getItem(position);
            if (dataBean != null) {
                Intent intent = new Intent(AuthUserDetailActivity.this, SharedUserDetailActivity.class);
                intent.putExtra(Constant.PRE_A, Constant.AUTH_USER_DETAIL_A);
                intent.putExtra(Constant.SHARE_USER_DEVICE_DATA, dataBean);
                intent.putExtra(Constant.SHARE_USER_DATA, mShareUser);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            showRemoveUserDialog();
        });
        mDevicesAdapter.setOnReInviteListener(this::share);
        rvLockList.setLayoutManager(new LinearLayoutManager(this));
        rvLockList.setAdapter(mDevicesAdapter);
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
        mTvDeviceNum.setText(String.valueOf(mDevicesAdapter.getItemCount()));
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        //缓存
                .skipMemoryCache(false)
                .error(R.drawable.home_user_authorization_user);
        Glide.with(this)
                .load(TextUtils.isEmpty(mShareUser.getAvatarPath()) ? "" : mShareUser.getAvatarPath())
                .apply(requestOptions)
                .into(ivAvatar);
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
                    List<GetAllSharedUserFromLockBeanRsp.DataBean> dataBeans = getDevicesFromUidAndSharedUidBeanRsp.getData();
                    if (dataBeans != null && !dataBeans.isEmpty()) {
                        mDevicesAdapter.setList(dataBeans);
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

    private void share(GetAllSharedUserFromLockBeanRsp.DataBean dataBean) {
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
        req.setDeviceSN(dataBean.getLockNickname());
        req.setShareUserType(dataBean.getShareUserType());
        req.setAdminUid(uid);
        req.setFirstName(mShareUser.getFirstName());
        req.setLastName(mShareUser.getLastName());
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
}
