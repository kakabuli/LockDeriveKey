package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.UserListAdapter;
import com.revolo.lock.base.BaseActivity;
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
        if (getContext() != null) {
            new TitleBar(root).setTitle(getString(R.string.title_user))
                    .setRight(R.drawable.ic_home_icon_add, v -> {
                        Intent intent = new Intent(getContext(), AddDeviceForSharedUserActivity.class);
                        startActivity(intent);
                    });
            mLlNoUser = root.findViewById(R.id.llNoUser);
            mRvLockList = root.findViewById(R.id.rvLockList);
            mRvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
            mUserListAdapter = new UserListAdapter(R.layout.item_user_list_rv);
            mUserListAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    Intent intent = new Intent(getActivity(), AuthUserDetailActivity.class);
                    intent.putExtra(Constant.SHARED_USER_DATA, mUserListAdapter.getItem(position));
                    startActivity(intent);
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

    @Override
    public void onStart() {
        super.onStart();
    }

    private void getAllSharedUserFromAdminUser() {
//        if (getActivity() instanceof BaseActivity) {
//            if (!((BaseActivity) getActivity()).checkNetConnectFail()) {
//                return;
//            }
//        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("getAllSharedUserFromAdminUser App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("getAllSharedUserFromAdminUser uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
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
            public void onNext(@NonNull GetAllSharedUserFromAdminUserBeanRsp userBeanRsp) {
                dismissLoading();
                String code = userBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("getAllSharedUserFromAdminUser code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, getActivity());
                        return;
                    }
                    String msg = userBeanRsp.getMsg();
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    Timber.e("getAllSharedUserFromAdminUser code: %1s, msg: %2s", code, userBeanRsp.getMsg());
                    return;
                }
                if (userBeanRsp.getData() == null) {
                    mLlNoUser.setVisibility(View.VISIBLE);
                    mRvLockList.setVisibility(View.GONE);
                    return;
                }
                if (userBeanRsp.getData().isEmpty()) {
                    mLlNoUser.setVisibility(View.VISIBLE);
                    mRvLockList.setVisibility(View.GONE);
                } else {
                    mLlNoUser.setVisibility(View.GONE);
                    mRvLockList.setVisibility(View.VISIBLE);
                }
                mUserListAdapter.setList(userBeanRsp.getData());
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
        mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    public void showLoading(@NotNull String message) {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        }
        mLoadingDialog = new CustomerLoadingDialog.Builder(getContext())
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
        mLoadingDialog.show();
    }

    public void showLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }
}