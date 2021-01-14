package com.revolo.lock.ui.home.device;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.R;
import com.revolo.lock.adapter.SharedUserListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.TestUserManagementBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户管理
 */
public class UserManagementActivity extends BaseActivity {

    private SharedUserListAdapter mSharedUserListAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

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
                            // TODO: 2021/1/14 add user
                        });
        RecyclerView rvSharedUser = findViewById(R.id.rvSharedUser);
        mSharedUserListAdapter = new SharedUserListAdapter(R.layout.item_shared_user_rv);
        rvSharedUser.setLayoutManager(new LinearLayoutManager(this));
        rvSharedUser.setAdapter(mSharedUserListAdapter);

    }

    @Override
    public void doBusiness() {
        initTestUserData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initTestUserData() {

        ((TextView) findViewById(R.id.tvUserShared)).setText("users Harry's Shared");

        List<TestUserManagementBean> beanList = new ArrayList<>();
        TestUserManagementBean bean1 = new TestUserManagementBean("Jack", 1, 1);
        beanList.add(bean1);
        TestUserManagementBean bean2 = new TestUserManagementBean("Marry", 2, 2);
        beanList.add(bean2);
        TestUserManagementBean bean3 = new TestUserManagementBean("Jim", 1, 3);
        beanList.add(bean3);
        TestUserManagementBean bean4 = new TestUserManagementBean("Tick", 2, 3);
        beanList.add(bean4);

        mSharedUserListAdapter.setList(beanList);
    }

}
