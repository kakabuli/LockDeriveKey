package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.SharedUserListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.ShareUserDetailBean;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.request.GetAllSharedUserFromLockBeanReq;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.user.InviteUsersMailActivity;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户管理
 */
public class UserManagementActivity extends BaseActivity {

    private SharedUserListAdapter mSharedUserListAdapter;
    private BleDeviceLocal mBleDeviceLocal;
    private RecyclerView mRvSharedUser;
    private LinearLayout mLinearLayout;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        // TODO: 2021/3/8 处理
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_user_management;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_user_management))
                .setRight(R.drawable.ic_home_icon_add,
                        v -> {
                            Intent intent = new Intent(this, InviteUsersMailActivity.class);
                            intent.putExtra(Constant.LOCK_ESN, mBleDeviceLocal.getEsn());
                            intent.putExtra(Constant.PRE_A, Constant.USER_MANAGEMENT_A);
                            startActivity(intent);
                        });
        mRvSharedUser = findViewById(R.id.rvSharedUser);
        mLinearLayout = findViewById(R.id.linearLayout);
        mSharedUserListAdapter = new SharedUserListAdapter(R.layout.item_shared_user_rv);
        mSharedUserListAdapter.setOnItemClickListener((adapter, view, position) -> {
            GetAllSharedUserFromLockBeanRsp.DataBean dataBean = mSharedUserListAdapter.getItem(position);
            if (dataBean != null) {
                int shareType = dataBean.getShareUserType();
                if (shareType == 3 || shareType == 4 || shareType == 5) {
                    return;
                }

                ShareUserDetailBean shareUserDetailBean = new ShareUserDetailBean();
                shareUserDetailBean.setAvatar(dataBean.getAvatarPath());
                shareUserDetailBean.setName(dataBean.getFirstName() + " " + dataBean.getLastName());
                shareUserDetailBean.setIsEnable(dataBean.getIsEnable());
                shareUserDetailBean.setShareId(dataBean.getShareId());
                shareUserDetailBean.setShareUserType(dataBean.getShareUserType());

                Intent intent = new Intent(UserManagementActivity.this, SharedUserDetailActivity.class);
                intent.putExtra(Constant.SHARE_USER_DATA, shareUserDetailBean);
                startActivity(intent);
            }
        });
        mSharedUserListAdapter.setOnReInviteListener(this::share);
        mRvSharedUser.setLayoutManager(new LinearLayoutManager(this));
        mRvSharedUser.setAdapter(mSharedUserListAdapter);
        mSharedUserListAdapter.setEmptyView(R.layout.empty_view_share_user_list);
        initLoading(getString(R.string.t_load_content_loading));
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
    public void doBusiness() {
        mLinearLayout.setVisibility(View.GONE);
        String firstName = App.getInstance().getUser().getFirstName();
        ((TextView) findViewById(R.id.tvUserShared)).setText((TextUtils.isEmpty(firstName) ? "" : firstName) + "'s Shared");
        getAllSharedUserFromLock();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void share(@NonNull GetAllSharedUserFromLockBeanRsp.DataBean dataBean) {
        if (!dataBean.getShareState().equals("3")) { // 非等待状态不能分享
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
        req.setDeviceSN(mBleDeviceLocal.getEsn());
        req.setShareUserType(dataBean.getShareUserType());
        req.setAdminUid(uid);
        req.setFirstName(dataBean.getFirstName());
        req.setLastName(dataBean.getLastName());
        req.setShareAccount(dataBean.getNickName());
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
                    getAllSharedUserFromLock();
                } else if (code.equals("444")) {
                    App.getInstance().logout(true, UserManagementActivity.this);
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

    private void getAllSharedUserFromLock() {
        if (!checkNetConnectFail()) {
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("getAllSharedUserFromLock App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("getAllSharedUserFromLock uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("getAllSharedUserFromLock token is empty");
            return;
        }
        GetAllSharedUserFromLockBeanReq req = new GetAllSharedUserFromLockBeanReq();
        req.setUid(uid);
        req.setDeviceSN(mBleDeviceLocal.getEsn());
        showLoading();
        Observable<GetAllSharedUserFromLockBeanRsp> observable = HttpRequest.getInstance().getAllSharedUserFromLock(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetAllSharedUserFromLockBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetAllSharedUserFromLockBeanRsp rsp) {
                dismissLoading();
                String code = rsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("getAllSharedUserFromLock code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, UserManagementActivity.this);
                        return;
                    }
                    Timber.e("getAllSharedUserFromLock code: %1s, msg: %2s", code, rsp.getMsg());
                    return;
                }
                if (rsp.getData() == null) {
                    mLinearLayout.setVisibility(View.GONE);
                    return;
                }
                if (rsp.getData().isEmpty()) {
                    mLinearLayout.setVisibility(View.GONE);
                } else {
                    mLinearLayout.setVisibility(View.VISIBLE);
                }
                mSharedUserListAdapter.setList(rsp.getData());
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
