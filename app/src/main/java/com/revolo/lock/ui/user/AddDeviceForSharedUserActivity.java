package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDeviceAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestAuthDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   :为已分享的用户添加新设备
 */
public class AddDeviceForSharedUserActivity extends BaseActivity {

    private AuthUserDeviceAdapter mAuthUserDeviceAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device_for_shared_user;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_select_authorized_device));
        RecyclerView rvDevice = findViewById(R.id.rvDevice);
        mAuthUserDeviceAdapter = new AuthUserDeviceAdapter(R.layout.item_auth_user_device_rv);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        mAuthUserDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                startActivity(new Intent(AddDeviceForSharedUserActivity.this, SelectAuthorizedDeviceActivity.class));
            }
        });
        rvDevice.setAdapter(mAuthUserDeviceAdapter);
    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initTestData() {
        List<TestAuthDeviceBean> beanList = new ArrayList<>();
        TestAuthDeviceBean bean1 = new TestAuthDeviceBean("fly dream", "SN156123454");
        beanList.add(bean1);
        TestAuthDeviceBean bean2 = new TestAuthDeviceBean("Hello world", "SN156123484");
        beanList.add(bean2);
        TestAuthDeviceBean bean3 = new TestAuthDeviceBean("say something", "SN156122534");
        beanList.add(bean3);
        TestAuthDeviceBean bean4 = new TestAuthDeviceBean("Goodbye", "SN156145354");
        beanList.add(bean4);
        TestAuthDeviceBean bean5 = new TestAuthDeviceBean("Thinking", "SN156127531");
        beanList.add(bean5);
        TestAuthDeviceBean bean6 = new TestAuthDeviceBean("Hallo", "SN156123858");
        beanList.add(bean6);
        TestAuthDeviceBean bean7 = new TestAuthDeviceBean("Hey", "SN156123492");
        beanList.add(bean7);
        TestAuthDeviceBean bean8 = new TestAuthDeviceBean("Hours", "SN156123442");
        beanList.add(bean8);
        mAuthUserDeviceAdapter.setList(beanList);

    }

}
