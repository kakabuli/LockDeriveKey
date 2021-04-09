package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDetailDevicesAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GetDevicesFromUidAndSharedUidBeanReq;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;
import com.revolo.lock.bean.test.TestAuthUserBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.device.lock.SharedUserDetailActivity;

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

    private AuthUserDetailDevicesAdapter mDevicesAdapter;
    private TextView mTvDeviceNum, mTvAccount, mTvUserName;
    private GetAllSharedUserFromAdminUserBeanRsp.DataBean mSharedUserData;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.SHARED_USER_DATA)) {
            mSharedUserData = intent.getParcelableExtra(Constant.SHARED_USER_DATA);
        }
        if(mSharedUserData == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auth_user_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_users))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthUserDetailActivity.this, AddDeviceForSharedUserActivity.class);
                startActivity(intent);
            }
        });
        RecyclerView rvLockList = findViewById(R.id.rvLockList);
        mTvDeviceNum = findViewById(R.id.tvDeviceNum);
        mTvUserName = findViewById(R.id.tvUserName);
        mTvAccount = findViewById(R.id.tvAccount);
        mDevicesAdapter = new AuthUserDetailDevicesAdapter(R.layout.item_user_devices_rv);
        mDevicesAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if(position >= 0 && adapter.getItem(position) != null) {
                    TestAuthUserBean.TestDeviceBean bean = (TestAuthUserBean.TestDeviceBean) adapter.getItem(position);
                    if(bean.getState() == 1 || bean.getState() == 2) {
                        // TODO: 2021/1/15 弹出分享链接重新邀请
                    } else {
                        Intent intent = new Intent(AuthUserDetailActivity.this, SharedUserDetailActivity.class);
                        intent.putExtra(Constant.PRE_A, Constant.AUTH_USER_DETAIL_A);
                        startActivity(intent);
                    }
                }
                
            }
        });
        rvLockList.setLayoutManager(new LinearLayoutManager(this));
        rvLockList.setAdapter(mDevicesAdapter);
        applyDebouncingClickListener(findViewById(R.id.clUserName));
        initLoading("Loading...");

    }

    @Override
    public void doBusiness() {
        refreshUI();
        searchUserDevice();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clUserName) {
            Intent intent = new Intent(this, ChangeSharedUserNameActivity.class);
            intent.putExtra(Constant.SHARED_USER_DATA, mSharedUserData);
            startActivity(intent);
        }
    }

    private void refreshUI() {
        // TODO: 2021/3/8 后面用邮箱
        mTvAccount.setText(mSharedUserData.getUserNickname());
        mTvUserName.setText(mSharedUserData.getUserNickname());
        int num = mDevicesAdapter.getData().size();
        mTvDeviceNum.setText(StringUtils.format("%1d", num));
    }

    private void searchUserDevice() {
        if(!checkNetConnectFail()) {
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("searchUserDevice App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("searchUserDevice token is empty");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("searchUserDevice uid is empty");
            return;
        }

        GetDevicesFromUidAndSharedUidBeanReq req = new GetDevicesFromUidAndSharedUidBeanReq();
        req.setUid(uid);
        req.setShareId(mSharedUserData.getUid());
        showLoading();
        Observable<GetDevicesFromUidAndSharedUidBeanRsp> observable = HttpRequest.getInstance().getDevicesFromUidAndSharedUid(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetDevicesFromUidAndSharedUidBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetDevicesFromUidAndSharedUidBeanRsp getDevicesFromUidAndSharedUidBeanRsp) {
                String code = getDevicesFromUidAndSharedUidBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("searchUserDevice code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, AuthUserDetailActivity.this);
                        return;
                    }
                    String msg = getDevicesFromUidAndSharedUidBeanRsp.getMsg();
                    Timber.e("searchUserDevice code: %1s, msg: %2s", code, msg);
                    if(!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> dataBeans = getDevicesFromUidAndSharedUidBeanRsp.getData();
                if(dataBeans == null) {
                    return;
                }
                runOnUiThread(() -> mDevicesAdapter.setList(dataBeans));
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
