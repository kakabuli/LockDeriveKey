package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.R;
import com.revolo.lock.adapter.UserListAdapter;
import com.revolo.lock.bean.test.TestUserManagementBean;
import com.revolo.lock.ui.TitleBar;

import java.util.List;

public class UserFragment extends Fragment {

    private UserViewModel mUserViewModel;

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
            RecyclerView rvLockList = root.findViewById(R.id.rvLockList);
            rvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
            final UserListAdapter userListAdapter = new UserListAdapter(R.layout.item_user_list_rv);
            userListAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    startActivity(new Intent(getActivity(), AuthUserDetailActivity.class));
                }
            });
            rvLockList.setAdapter(userListAdapter);
            mUserViewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<List<TestUserManagementBean>>() {
                @Override
                public void onChanged(List<TestUserManagementBean> testUserManagementBeans) {
                    userListAdapter.setList(testUserManagementBeans);
                }
            });
        }
        return root;
    }
}