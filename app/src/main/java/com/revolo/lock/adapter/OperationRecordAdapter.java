package com.revolo.lock.adapter;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.showBean.RecordState;
import com.revolo.lock.bean.test.OperationRecords;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.revolo.lock.bean.showBean.RecordState.DOOR_CLOSED_DETECTED;
import static com.revolo.lock.bean.showBean.RecordState.DOOR_OPENED_DETECTED;
import static com.revolo.lock.bean.showBean.RecordState.DOUBLE_LOCK_INSIDE_THE_DOOR;
import static com.revolo.lock.bean.showBean.RecordState.DURESS_PASSWORD_UNLOCK;
import static com.revolo.lock.bean.showBean.RecordState.JAM_ALARM;
import static com.revolo.lock.bean.showBean.RecordState.LOCKING_INSIDE_THE_DOOR;
import static com.revolo.lock.bean.showBean.RecordState.LOCK_DOWN_ALARM;
import static com.revolo.lock.bean.showBean.RecordState.LOCK_RESTORE;
import static com.revolo.lock.bean.showBean.RecordState.LOW_BATTERY_ALARM;
import static com.revolo.lock.bean.showBean.RecordState.MULTI_FUNCTIONAL_BUTTON_LOCKING;
import static com.revolo.lock.bean.showBean.RecordState.ONE_TOUCH_LOCK_OUTSIDE_THE_DOOR;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_LOCKED_THE_DOOR_BY_APP;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_LOCKED_THE_DOOR_BY_MECHANICAL_KEY;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_USE_A_PWD_TO_UNLOCK;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_USE_GEO_FENCE_TO_UNLOCK;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK;
import static com.revolo.lock.bean.showBean.RecordState.SOMEONE_USE_THE_APP_TO_UNLOCK;
import static com.revolo.lock.bean.showBean.RecordState.THE_USER_ADDED_A_PWD;
import static com.revolo.lock.bean.showBean.RecordState.THE_USER_ADDED_SOMEONE_IN_FAMILY_GROUP;
import static com.revolo.lock.bean.showBean.RecordState.THE_USER_DELETED_A_PWD;
import static com.revolo.lock.bean.showBean.RecordState.THE_USER_REMOVED_SOMEONE_FROM_FAMILY_GROUP;
import static com.revolo.lock.bean.showBean.RecordState.USER_ADDED_SOMEONE_AS_GUEST_USER;
import static com.revolo.lock.bean.showBean.RecordState.USER_REMOVED_SOMEONE_FROM_GUEST_USER;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 当前日期的操作记录
 */
public class OperationRecordAdapter extends BaseQuickAdapter<OperationRecords.OperationRecord, BaseViewHolder> {

    public OperationRecordAdapter(int layoutResId, @Nullable List<OperationRecords.OperationRecord> data) {
        super(layoutResId, data);
    }

    public OperationRecordAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, OperationRecords.OperationRecord operationRecord) {
        if(operationRecord != null) {
            baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.c333333));
            @DrawableRes int imageResId = R.drawable.ic_home_log_icon__password;
            @RecordState.OpRecordState int state = operationRecord.getState();
            switch (state) {
                case SOMEONE_USE_A_PWD_TO_UNLOCK:
                case THE_USER_ADDED_A_PWD:
                case THE_USER_DELETED_A_PWD:
                    imageResId = R.drawable.ic_home_log_icon__password;
                    break;
                case SOMEONE_USE_GEO_FENCE_TO_UNLOCK:
                    imageResId = R.drawable.ic_home_log_icon__geofence;
                    break;
                case SOMEONE_USE_THE_APP_TO_UNLOCK:
                case SOMEONE_LOCKED_THE_DOOR_BY_APP:
                    imageResId = R.drawable.ic_home_log_icon__iphone;
                    break;
                case SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK:
                case SOMEONE_LOCKED_THE_DOOR_BY_MECHANICAL_KEY:
                    imageResId = R.drawable.ic_home_log_icon__key;
                    break;
                case LOCKING_INSIDE_THE_DOOR:
                case DOUBLE_LOCK_INSIDE_THE_DOOR:
                case MULTI_FUNCTIONAL_BUTTON_LOCKING:
                case ONE_TOUCH_LOCK_OUTSIDE_THE_DOOR:
                    imageResId = R.drawable.ic_home_log_icon_door_lock;
                    break;
                case DURESS_PASSWORD_UNLOCK:
                case LOCK_DOWN_ALARM:
                case JAM_ALARM:
                    imageResId = R.drawable.ic_home_log_icon__alert;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case LOW_BATTERY_ALARM:
                    imageResId = R.drawable.ic_home_log_icon__battery;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case DOOR_OPENED_DETECTED:
                    imageResId = R.drawable.ic_home_log_icon__door_open;
                    break;
                case DOOR_CLOSED_DETECTED:
                    imageResId = R.drawable.ic_home_log_icon__door_close;
                    break;
                case THE_USER_ADDED_SOMEONE_IN_FAMILY_GROUP:
                case USER_ADDED_SOMEONE_AS_GUEST_USER:
                    imageResId = R.drawable.ic_home_log_icon__add;
                    break;
                case THE_USER_REMOVED_SOMEONE_FROM_FAMILY_GROUP:
                case USER_REMOVED_SOMEONE_FROM_GUEST_USER:
                    imageResId = R.drawable.ic_home_log_icon__del;
                    break;
                case LOCK_RESTORE:
                    imageResId = R.drawable.ic_home_log_icon__restore;
                    break;
            }
            baseViewHolder.setImageResource(R.id.ivLogState, imageResId);
            baseViewHolder.setText(R.id.tvMessage, operationRecord.getMessage());
            baseViewHolder.setText(R.id.tvTime, TimeUtils.millis2String(operationRecord.getOperationTime(), "HH:mm"));
        }
    }
}
