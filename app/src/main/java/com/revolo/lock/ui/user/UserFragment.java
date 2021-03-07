package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.UserListAdapter;
import com.revolo.lock.bean.request.GetAllSharedUserFromAdminUserBeanReq;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.dialog.iosloading.CustomerLoadingDialog;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.TitleBar;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class UserFragment extends Fragment {

    private UserViewModel mUserViewModel;
    private CustomerLoadingDialog mLoadingDialog;
    private UserListAdapter mUserListAdapter;
    private LinearLayout mLlNoUser;
    private RecyclerView mRvLockList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mUserViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        if(getContext() != null) {
            new TitleBar(root).setTitle(getString(R.string.title_user))
                    .setRight(ContextCompat.getDrawable(getContext(), R.drawable.ic_home_icon_add), v -> {
                        // TODO: 2021/1/15 添加用户
                    });
            mLlNoUser = root.findViewById(R.id.llNoUser);
            mRvLockList = root.findViewById(R.id.rvLockList);
            mRvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
            mUserListAdapter = new UserListAdapter(R.layout.item_user_list_rv);
            mUserListAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    startActivity(new Intent(getActivity(), AuthUserDetailActivity.class));
                }
            });
            mRvLockList.setAdapter(mUserListAdapter);
//            mUserViewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<List<TestUserManagementBean>>() {
//                @Override
//                public void onChanged(List<TestUserManagementBean> testUserManagementBeans) {
//                    userListAdapter.setList(testUserManagementBeans);
//                }
//            });
        }
        initLoading("Loading...");
        mLlNoUser.setVisibility(View.VISIBLE);
        mRvLockList.setVisibility(View.GONE);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllSharedUserFromAdminUser();
    }

    private void getAllSharedUserFromAdminUser() {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("getAllSharedUserFromAdminUser App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("getAllSharedUserFromAdminUser uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("getAllSharedUserFromAdminUser token is empty");
            return;
        }
        GetAllSharedUserFromAdminUserBeanReq req = new GetAllSharedUserFromAdminUserBeanReq();
        req.setUid(uid);
        showLoading();
        Observable<GetAllSharedUserFromAdminUserBeanRsp> observable = HttpRequest.getInstance().getAllSharedUserFromAdminUser(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GetAllSharedUserFromAdminUserBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GetAllSharedUserFromAdminUserBeanRsp getAllSharedUserFromAdminUserBeanRsp) {
                dismissLoading();
                String code = getAllSharedUserFromAdminUserBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("getAllSharedUserFromAdminUser code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    Timber.e("getAllSharedUserFromAdminUser code: %1s, msg: %2s", code, getAllSharedUserFromAdminUserBeanRsp.getMsg());
                    return;
                }
                if(getAllSharedUserFromAdminUserBeanRsp.getData() == null) {
                    mLlNoUser.setVisibility(View.VISIBLE);
                    mRvLockList.setVisibility(View.GONE);
                    return;
                }
                if(getAllSharedUserFromAdminUserBeanRsp.getData().isEmpty()) {
                    mLlNoUser.setVisibility(View.VISIBLE);
                    mRvLockList.setVisibility(View.GONE);
                } else {
                    mLlNoUser.setVisibility(View.GONE);
                    mRvLockList.setVisibility(View.VISIBLE);
                }
                mUserListAdapter.setList(getAllSharedUserFromAdminUserBeanRsp.getData());
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

    public void initLoading(String message) {
        // TODO: 2021/2/25 抽离文字
        mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    // TODO: 2021/3/4 修改抽离文字
    public void showLoading(@NotNull String message) {
        if(mLoadingDialog != null) {
            if(mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        }
        // TODO: 2021/2/25 抽离文字
        mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
        mLoadingDialog.show();
    }

    public void showLoading() {
        if(mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoading() {
        if(mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }
}