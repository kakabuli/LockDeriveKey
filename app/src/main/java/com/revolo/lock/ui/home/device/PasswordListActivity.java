package com.revolo.lock.ui.home.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.R;
import com.revolo.lock.adapter.PasswordListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.TestPwdBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表界面
 */
public class PasswordListActivity extends BaseActivity {

    private PasswordListAdapter mPasswordListAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.password))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add),
                        v -> startActivity(new Intent(this, AddInputNewPwdActivity.class)));
        RecyclerView rvPwdList = findViewById(R.id.rvPwdList);
        rvPwdList.setLayoutManager(new LinearLayoutManager(this));
        mPasswordListAdapter = new PasswordListAdapter(R.layout.item_pwd_list_rv);
        rvPwdList.setAdapter(mPasswordListAdapter);
    }

    @Override
    public void doBusiness() {
        testInitPwd();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void testInitPwd() {
        List<TestPwdBean> list = new ArrayList<>();
        TestPwdBean testPwdBean1 = new TestPwdBean("Password name", "Permanent password", 1);
        list.add(testPwdBean1);
        TestPwdBean testPwdBean2 = new TestPwdBean("Password name", "Sun、Mon、Tues、Wed、Thure、Tir\n" +
                "15:00-17:00", 1);
        list.add(testPwdBean2);
        TestPwdBean testPwdBean3 = new TestPwdBean("Password name", "start: 12,28,2020   12:00 \n" +
                "end:  12,28,2020   16:00", 1);
        list.add(testPwdBean3);
        TestPwdBean testPwdBean4 = new TestPwdBean("Password name", "start: 12,28,2020   12:00 \n" +
                "end:  12,28,2020   16:00", 2);
        list.add(testPwdBean4);
        mPasswordListAdapter.setList(list);

    }

}
