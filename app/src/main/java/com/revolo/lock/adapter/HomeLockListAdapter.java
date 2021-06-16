package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.room.entity.BleDeviceLocal;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 首页锁列表适配器
 */
public class HomeLockListAdapter extends BaseQuickAdapter<BleDeviceLocal, BaseViewHolder> {

    public HomeLockListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, BleDeviceLocal deviceLocal) {
        if (deviceLocal == null) {
            return;
        }
        if (deviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_privacymodel);
            baseViewHolder.setText(R.id.tvDoorState, getContext().getString(R.string.tip_private_mode));
            baseViewHolder.setGone(R.id.ivDoorState, true);
        } else {

            baseViewHolder.setGone(R.id.ivDoorState, false);
            boolean isUseDoorSensor = deviceLocal.isOpenDoorSensor();
            ImageView ivDoorState = baseViewHolder.getView(R.id.ivDoorState);
            TextView tvDoorState = baseViewHolder.getView(R.id.tvDoorState);
            if (deviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_open);
                if (isUseDoorSensor) {
                    switch (deviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                        case LocalState.DOOR_SENSOR_OPEN:
                            // 因为异常，所以与锁的状态同步
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorOpen(ivDoorState, tvDoorState);
                }
            } else if (deviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE || deviceLocal.getLockState() == LocalState.LOCK_STATE_SENSOR_CLOSE) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_close);
                if (isUseDoorSensor) {
                    switch (deviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            // 因为异常，所以与锁的状态同步
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorClose(ivDoorState, tvDoorState);
                }
            } else {
                //异常处理
                Timber.e("homeLock type:%s", deviceLocal.getLockState() + "");
                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_privacymodel);
            }
        }
        if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_wifi);
        } else if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_bluetooth);
        } else if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {//WiFi和ble同时存在连接
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_wifi);
        } else if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_DIS) {//掉线模式else
            // TODO: 2021/3/2 其他处理
            baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_privacymodel);
        }
        String name = deviceLocal.getName();
        baseViewHolder.setText(R.id.tvLockName, TextUtils.isEmpty(name) ?
                (TextUtils.isEmpty(deviceLocal.getEsn()) ? "" : deviceLocal.getEsn())
                : name);
    }

    private void doorClose(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_door_closed);
    }

    private void doorOpen(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
        tvDoorState.setText(R.string.tip_door_opened);
    }

}
