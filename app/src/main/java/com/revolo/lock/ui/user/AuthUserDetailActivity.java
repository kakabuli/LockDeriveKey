package com.revolo.lock.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AuthUserDetailDevicesAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestAuthUserBean;
import com.revolo.lock.ui.device.lock.SharedUserDetailActivity;

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
                startActivity(new Intent(AuthUserDetailActivity.this, AddDeviceForSharedUserActivity.class));
            }
        });
        RecyclerView rvLockList = findViewById(R.id.rvLockList);
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

    }

    @Override
    public void doBusiness() {
        initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.clUserName) {
            startActivity(new Intent(this, ChangeSharedUserNameActivity.class));
        }
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
