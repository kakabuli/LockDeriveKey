package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 首页锁列表适配器
 */
public class HomeLockListAdapter extends BaseQuickAdapter<BleDeviceLocal, BaseViewHolder> {
    public HomeLockListAdapter(int layoutResId, @Nullable List<BleDeviceLocal> data) {
        super(layoutResId, data);
    }

    public HomeLockListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, BleDeviceLocal deviceLocal) {
        if(deviceLocal == null) {
            return;
        }
        if(deviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_privacymodel);
            baseViewHolder.setText(R.id.tvDoorState, getContext().getString(R.string.tip_private_mode));
            baseViewHolder.setGone(R.id.ivDoorState, true);
        } else {
            baseViewHolder.setGone(R.id.ivDoorState, false);
            if(deviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_open);
                baseViewHolder.setImageResource(R.id.ivDoorState, R.drawable.ic_home_icon_door_open);
                baseViewHolder.setText(R.id.tvDoorState, getContext().getString(R.string.tip_door_opened));
            } else if(deviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_close);
                baseViewHolder.setImageResource(R.id.ivDoorState, R.drawable.ic_home_icon_door_closed);
                baseViewHolder.setText(R.id.tvDoorState, getContext().getString(R.string.tip_door_closed));
            }
        }
        if(deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_wifi);
        } else if(deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_bluetooth);
        } else {
            // TODO: 2021/3/2 其他处理
        }
        String name = deviceLocal.getName();
        baseViewHolder.setText(R.id.tvLockName, TextUtils.isEmpty(name)?
                (TextUtils.isEmpty(deviceLocal.getEsn())?"":deviceLocal.getEsn())
                :name);
    }

}
