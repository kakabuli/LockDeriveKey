package com.revolo.lock.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.TestAuthDeviceBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户管理-授权用户-添加设备-选择设备
 */
public class AuthUserDeviceAdapter extends BaseQuickAdapter<TestAuthDeviceBean, BaseViewHolder> {
    public AuthUserDeviceAdapter(int layoutResId, @Nullable List<TestAuthDeviceBean> data) {
        super(layoutResId, data);
    }

    public AuthUserDeviceAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, TestAuthDeviceBean testAuthDeviceBean) {
        if(testAuthDeviceBean != null) {
            holder.setText(R.id.tvDeviceName, testAuthDeviceBean.getDeviceName());
            holder.setText(R.id.tvSn, testAuthDeviceBean.getDeviceSN());
        }
    }
}
