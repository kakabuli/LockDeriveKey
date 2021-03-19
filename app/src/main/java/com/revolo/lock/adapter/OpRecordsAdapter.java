package com.revolo.lock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.R;
import com.revolo.lock.bean.OperationRecords;
import com.revolo.lock.bean.showBean.RecordState;

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
 * author :
 * time   : 2021/3/18
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OpRecordsAdapter extends BaseExpandableListAdapter {

    List<OperationRecords> mOperationRecords;
    private final Context mContext;

    public OpRecordsAdapter(List<OperationRecords> operationRecords, Context context) {
        mOperationRecords = operationRecords;
        mContext = context;
    }

    public void setOperationRecords(List<OperationRecords> operationRecords) {
        mOperationRecords = operationRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mOperationRecords.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mOperationRecords.get(groupPosition).getOperationRecords().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mOperationRecords.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mOperationRecords.get(groupPosition).getOperationRecords().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_operation_record_list_rv, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTimeTitle = (TextView) convertView.findViewById(R.id.tvTimeTitle);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTimeTitle.setText(getDay(mOperationRecords.get(groupPosition).getTitleOperationTime()));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_operation_record_rv, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            childViewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            childViewHolder.ivLogState = (ImageView) convertView.findViewById(R.id.ivLogState);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        refreshUI(mOperationRecords.get(groupPosition).getOperationRecords().get(childPosition),
                childViewHolder.tvMessage, childViewHolder.ivLogState, childViewHolder.tvTime);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class GroupViewHolder {
        TextView tvTimeTitle;
    }
    static class ChildViewHolder {
        TextView tvMessage;
        ImageView ivLogState;
        TextView tvTime;
    }

    private String getDay(long time) {
        if(TimeUtils.isToday(time)) {
            return "Today";
        } else {
            // 减掉一天的时间
            if(TimeUtils.isToday(time+86400000)) {
                return "Yesterday";
            } else {
                return TimeUtils.millis2String(time, "MMM dd yyyy");
            }
        }
    }

    private void refreshUI(OperationRecords.OperationRecord operationRecord, TextView tvMessage, ImageView ivLogState,TextView tvTime) {
        tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.c333333));
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
                tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.cFF556D));
                break;
            case LOW_BATTERY_ALARM:
                imageResId = R.drawable.ic_home_log_icon__battery;
                tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.cFF556D));
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
        ivLogState.setImageResource(imageResId);
        tvMessage.setText(operationRecord.getMessage());
        tvTime.setText(TimeUtils.millis2String(operationRecord.getOperationTime(), "HH:mm"));
    }

}
