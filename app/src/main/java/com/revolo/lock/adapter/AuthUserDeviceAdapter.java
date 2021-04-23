package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestAuthDeviceBean;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户管理-授权用户-添加设备-选择设备
 */
public class AuthUserDeviceAdapter extends BaseQuickAdapter<BleDeviceLocal, BaseViewHolder> {
    public AuthUserDeviceAdapter(int layoutResId, @Nullable List<BleDeviceLocal> data) {
        super(layoutResId, data);
    }

    public AuthUserDeviceAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BleDeviceLocal local) {
        if(local != null) {
            String name = local.getName();
            String esn = local.getEsn();
            holder.setText(R.id.tvDeviceName, TextUtils.isEmpty(name)?esn:name);
            holder.setText(R.id.tvSn, TextUtils.isEmpty(esn)?"":getContext().getString(R.string.equipment_n_esn, esn));
        }
    }
}
