package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.Constant;
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
        ImageView ivDoorState = baseViewHolder.getView(R.id.ivDoorState);
        TextView tvDoorState = baseViewHolder.getView(R.id.tvDoorState);
        String name = deviceLocal.getName();
        String share = deviceLocal.getShareUserType() == 1 ? "(Family) " : deviceLocal.getShareUserType() == 2 ? "(Guest)" : "";
        baseViewHolder.setText(R.id.tvLockName, share + (TextUtils.isEmpty(name) ?
                (TextUtils.isEmpty(deviceLocal.getEsn()) ? "" : deviceLocal.getEsn())
                : name));
        if (deviceLocal.getLockState() == LocalState.LOCK_STATE_PRIVATE) {
            baseViewHolder.setImageResource(R.id.ivLockState, R.mipmap.ic_home_img_lock_privacymodel);
            doorShow(ivDoorState, tvDoorState);
            baseViewHolder.setText(R.id.tvDoorState, getContext().getString(R.string.tip_private_mode));
            baseViewHolder.setGone(R.id.ivDoorState, true);
        } else {
            baseViewHolder.setGone(R.id.ivDoorState, false);
            boolean isUseDoorSensor = deviceLocal.isOpenDoorSensor();
            if (deviceLocal.getLockState() == LocalState.LOCK_STATE_OPEN) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.mipmap.ic_home_img_lock_open);
                if (isUseDoorSensor) {
                    doorShow(ivDoorState, tvDoorState);
                    switch (deviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_CLOSE:
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            doorError(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            // 因为异常，所以与锁的状态同步
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorHide(ivDoorState, tvDoorState);
//                    doorClose(ivDoorState, tvDoorState);
                }
            } else if (deviceLocal.getLockState() == LocalState.LOCK_STATE_CLOSE || deviceLocal.getLockState() == LocalState.LOCK_STATE_SENSOR_CLOSE) {
                baseViewHolder.setImageResource(R.id.ivLockState, R.mipmap.ic_home_img_lock_close);
                if (isUseDoorSensor) {
                    doorShow(ivDoorState, tvDoorState);
                    switch (deviceLocal.getDoorSensor()) {
                        case LocalState.DOOR_SENSOR_EXCEPTION:
                        case LocalState.DOOR_SENSOR_INIT:
                            doorError(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_CLOSE:
                            // 因为异常，所以与锁的状态同步
                            doorClose(ivDoorState, tvDoorState);
                            break;
                        case LocalState.DOOR_SENSOR_OPEN:
                            doorOpen(ivDoorState, tvDoorState);
                            break;
                    }
                } else {
                    doorHide(ivDoorState, tvDoorState);
//                    doorClose(ivDoorState, tvDoorState);
                }
            } else {
                //异常处理
                Timber.e("homeLock type:%s", deviceLocal.getLockState() + "");
                baseViewHolder.setImageResource(R.id.ivLockState, R.mipmap.ic_home_img_lock_close);
//                baseViewHolder.setImageResource(R.id.ivLockState, R.drawable.ic_home_img_lock_privacymodel);
                doorClose(ivDoorState, tvDoorState);
            }
        }
        if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_wifi);
            baseViewHolder.setVisible(R.id.ivNetState, true);
        } else if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_bluetooth);
            baseViewHolder.setVisible(R.id.ivNetState, true);
        } else if (deviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {//WiFi和ble同时存在连接
            baseViewHolder.setImageResource(R.id.ivNetState, R.drawable.ic_home_icon_wifi);
            baseViewHolder.setVisible(R.id.ivNetState, true);
        } else {//掉线模式else
            // TODO: 2021/3/2 其他处理
            baseViewHolder.setImageResource(R.id.ivLockState, R.mipmap.ic_home_img_lock_privacymodel);
            baseViewHolder.setVisible(R.id.ivNetState, false);
        }
    }

    private void doorClose(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_door_closed);
        tvDoorState.setTextColor(getContext().getColor(R.color.c666666));
    }

    private void doorError(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_closed);
        tvDoorState.setText(R.string.tip_door_error);
        tvDoorState.setTextColor(getContext().getColor(R.color.cFF6A36));
    }

    private void doorOpen(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setImageResource(R.drawable.ic_home_icon_door_open);
        tvDoorState.setText(R.string.tip_door_opened);
        tvDoorState.setTextColor(getContext().getColor(R.color.c666666));
    }

    private void doorHide(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setVisibility(View.GONE);
        tvDoorState.setVisibility(View.GONE);
    }

    private void doorShow(ImageView ivDoorState, TextView tvDoorState) {
        ivDoorState.setVisibility(View.VISIBLE);
        tvDoorState.setVisibility(View.VISIBLE);
    }
}
