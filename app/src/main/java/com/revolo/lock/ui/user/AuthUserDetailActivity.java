package com.revolo.lock.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDetailDevicesAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.TestAuthUserBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户详情页面
 */
public class AuthUserDetailActivity extends BaseActivity {

    private AuthUserDetailDevicesAdapter mDevicesAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auth_user_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar("Authorization users")
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2021/1/15 添加设备
            }
        });
        RecyclerView rvLockList = findViewById(R.id.rvLockList);
        mDevicesAdapter = new AuthUserDetailDevicesAdapter(R.layout.item_user_devices_rv);
        rvLockList.setLayoutManager(new LinearLayoutManager(this));
        rvLockList.setAdapter(mDevicesAdapter);

    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initTestData() {
        if(mDevicesAdapter != null) {
            List<TestAuthUserBean.TestDeviceBean> data = new ArrayList<>();
            TestAuthUserBean.TestDeviceBean bean1 = new TestAuthUserBean.TestDeviceBean("wahh", 1, 1);
            data.add(bean1);
            TestAuthUserBean.TestDeviceBean bean2 = new TestAuthUserBean.TestDeviceBean("wahh", 2, 2);
            data.add(bean2);
            TestAuthUserBean.TestDeviceBean bean3 = new TestAuthUserBean.TestDeviceBean("wahh", 3, 1);
            data.add(bean3);
            TestAuthUserBean.TestDeviceBean bean4 = new TestAuthUserBean.TestDeviceBean("wahh", 3, 3);
            data.add(bean4);
            TestAuthUserBean.TestDeviceBean bean5 = new TestAuthUserBean.TestDeviceBean("wahh", 3, 2);
            data.add(bean5);
            TestAuthUserBean bean = new TestAuthUserBean("xxxx@gmial.com", data);
            ((TextView) findViewById(R.id.tvAccount)).setText(bean.getMailAddress());
            ((TextView) findViewById(R.id.tvUserName)).setText(bean.getMailAddress());
            ((TextView) findViewById(R.id.tvDeviceNum)).setText(StringUtils.format("%1d",data.size()));
            mDevicesAdapter.setList(data);
        }
    }
}
