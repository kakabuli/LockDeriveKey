package com.revolo.lock.adapter;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestOperationRecords;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 当前日期的操作记录
 */
public class OperationRecordAdapter extends BaseQuickAdapter<TestOperationRecords.TestOperationRecord, BaseViewHolder> {
    public OperationRecordAdapter(int layoutResId, @Nullable List<TestOperationRecords.TestOperationRecord> data) {
        super(layoutResId, data);
    }

    public OperationRecordAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TestOperationRecords.TestOperationRecord testOperationRecord) {
        if(testOperationRecord != null) {
            baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.c333333));
            String message = "";
            @DrawableRes int imageResId;
            switch (testOperationRecord.getState()) {
                case 1:
                    message = getContext().getString(R.string.log_uses_a_password_to_unlock, "Ming");
                    imageResId = R.drawable.ic_home_log_icon__password;
                    break;
                case 2:
                    message = getContext().getString(R.string.log_uses_geo_fence_to_unlock, "John");
                    imageResId = R.drawable.ic_home_log_icon__geofence;
                    break;
                case 3:
                    message = getContext().getString(R.string.log_uses_the_app_to_unlock, "Han");
                    imageResId = R.drawable.ic_home_log_icon__iphone;
                    break;
                case 4:
                    message = getContext().getString(R.string.log_uses_mechanical_key_to_unlock, "Jack");
                    imageResId = R.drawable.ic_home_log_icon__key;
                    break;
                case 5:
                    message = getContext().getString(R.string.log_locking_inside_the_door);
                    imageResId = R.drawable.ic_home_log_icon_door_lock;
                    break;
                case 6:
                    message = getContext().getString(R.string.log_double_lock_inside_the_door);
                    imageResId = R.drawable.ic_home_log_icon_door_lock;
                    break;
                case 7:
                    message = getContext().getString(R.string.log_multi_functional_button_locking);
                    imageResId = R.drawable.ic_home_log_icon_door_lock;
                    break;
                case 8:
                    message = getContext().getString(R.string.log_one_touch_lock_outside_the_door);
                    imageResId = R.drawable.ic_home_log_icon_door_lock;
                    break;
                case 9:
                    message = getContext().getString(R.string.log_locked_the_door_by_app, "Amy");
                    imageResId = R.drawable.ic_home_log_icon__iphone;
                    break;
                case 10:
                    message = getContext().getString(R.string.log_locked_the_door_by_mechanical_key, "Ming");
                    imageResId = R.drawable.ic_home_log_icon__key;
                    break;
                case 11:
                    message = getContext().getString(R.string.log_duress_password_unlock);
                    imageResId = R.drawable.ic_home_log_icon__alert;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case 12:
                    message = getContext().getString(R.string.log_lock_down_alarm);
                    imageResId = R.drawable.ic_home_log_icon__alert;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case 13:
                    message = getContext().getString(R.string.log_low_battery_alarm);
                    imageResId = R.drawable.ic_home_log_icon__battery;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case 14:
                    message = getContext().getString(R.string.log_jam_alarm);
                    imageResId = R.drawable.ic_home_log_icon__alert;
                    baseViewHolder.setTextColor(R.id.tvMessage, ContextCompat.getColor(getContext(), R.color.cFF556D));
                    break;
                case 15:
                    message = getContext().getString(R.string.log_door_opened_detected);
                    imageResId = R.drawable.ic_home_log_icon__door_open;
                    break;
                case 16:
                    message = getContext().getString(R.string.log_door_closed_detected);
                    imageResId = R.drawable.ic_home_log_icon__door_close;
                    break;
                case 17:
                    message = getContext().getString(R.string.log_the_user_added_a_password);
                    imageResId = R.drawable.ic_home_log_icon__password;
                    break;
                case 18:
                    message = getContext().getString(R.string.log_the_user_deleted_a_password);
                    imageResId = R.drawable.ic_home_log_icon__password;
                    break;
                case 19:
                    message = getContext().getString(R.string.log_the_user_added_someone_in_family_group, "Ming");
                    imageResId = R.drawable.ic_home_log_icon__add;
                    break;
                case 20:
                    message = getContext().getString(R.string.log_the_user_removed_someone_from_family_group, "Ming");
                    imageResId = R.drawable.ic_home_log_icon__del;
                    break;
                case 21:
                    message = getContext().getString(R.string.log_user_added_aa_as_guest_user, "Ming");
                    imageResId = R.drawable.ic_home_log_icon__add;
                    break;
                case 22:
                    message = getContext().getString(R.string.log_user_removed_aa_from_guest_user, "Tai");
                    imageResId = R.drawable.ic_home_log_icon__del;
                    break;
                default:
                    imageResId = R.drawable.ic_home_log_icon__password;
                    break;
            }
            baseViewHolder.setImageResource(R.id.ivLogState, imageResId);
            baseViewHolder.setText(R.id.tvMessage, message);
            baseViewHolder.setText(R.id.tvTime, TimeUtils.millis2String(testOperationRecord.getOperationTime(), "HH:mm"));
        }
    }
}
