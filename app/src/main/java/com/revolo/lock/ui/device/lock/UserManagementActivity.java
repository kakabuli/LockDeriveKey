package com.revolo.lock.ui.device.lock;

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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.SharedUserListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GetAllSharedUserFromLockBeanReq;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.test.TestUserManagementBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.LOCK_DETAIL)) {
            mBleDeviceLocal = intent.getParcelableExtra(Constant.LOCK_DETAIL);
        }
        // TODO: 2021/3/8 处理
        if(mBleDeviceLocal == null) {
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
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add),
                        v -> {
                            Intent intent = new Intent(this, AuthorizationManagementActivity.class);
                            intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
                            startActivity(intent);
                        });
        mRvSharedUser = findViewById(R.id.rvSharedUser);
        mSharedUserListAdapter = new SharedUserListAdapter(R.layout.item_shared_user_rv);
        mSharedUserListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if(position < 0) {
                    Timber.e("position: %1d", position);
                    return;
                }
                Intent intent = new Intent(UserManagementActivity.this, SharedUserDetailActivity.class);
                intent.putExtra(Constant.PRE_A, Constant.USER_MANAGEMENT_A);
                intent.putExtra(Constant.LOCK_DETAIL, mBleDeviceLocal);
                intent.putExtra(Constant.SHARED_USER_DATA, mSharedUserListAdapter.getItem(position));
                startActivity(intent);
            }
        });
        mRvSharedUser.setLayoutManager(new LinearLayoutManager(this));
        mRvSharedUser.setAdapter(mSharedUserListAdapter);
        initLoading("Loading...");
    }

    @Override
    public void doBusiness() {
        String firstName = App.getInstance().getUser().getFirstName();
        ((TextView) findViewById(R.id.tvUserShared)).setText((TextUtils.isEmpty(firstName)?"":firstName)+"'s Shared");
        getAllSharedUserFromLock();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void getAllSharedUserFromLock() {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("getAllSharedUserFromLock App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("getAllSharedUserFromLock uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
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
                if(TextUtils.isEmpty(code)) {
                    Timber.e("getAllSharedUserFromLock code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    Timber.e("getAllSharedUserFromLock code: %1s, msg: %2s", code, rsp.getMsg());
                    return;
                }
                if(rsp.getData() == null) {
                    mRvSharedUser.setVisibility(View.GONE);
                    return;
                }
                if(rsp.getData().isEmpty()) {
                    mRvSharedUser.setVisibility(View.GONE);
                } else {
                    mRvSharedUser.setVisibility(View.VISIBLE);
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
