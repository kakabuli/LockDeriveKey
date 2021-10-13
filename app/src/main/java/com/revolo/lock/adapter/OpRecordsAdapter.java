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

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.R;
import com.revolo.lock.bean.OperationRecords;
import com.revolo.lock.util.ZoneUtil;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * author : Jack
 * time   : 2021/3/18
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
public class OpRecordsAdapter extends BaseExpandableListAdapter {

    List<OperationRecords> mOperationRecords;
    private final Context mContext;
    //时区
    private String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

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
            groupViewHolder.tvTimeTitle = convertView.findViewById(R.id.tvTimeTitle);
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
            childViewHolder.tvMessage = convertView.findViewById(R.id.tvMessage);
            childViewHolder.tvTime = convertView.findViewById(R.id.tvTime);
            childViewHolder.ivLogState = convertView.findViewById(R.id.ivLogState);
            childViewHolder.vPoint = convertView.findViewById(R.id.v_point);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        refreshUI(mOperationRecords.get(groupPosition).getOperationRecords().get(childPosition),
                childViewHolder.tvMessage, childViewHolder.ivLogState, childViewHolder.tvTime, childViewHolder.vPoint);
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
        View vPoint;
    }

    private String getDay(long time) {
        if (isToday(time)) {
            return "Today";
        } else {
            // 减掉一天的时间
            if (isToday(time + 86400000)) {
                return "Yesterday";
            } else {
                String strTime = ZoneUtil.getDate(timeZone, time, "dd/MM/yyyy");
                String subStartTime = strTime.substring(0, 2);
                String replaceStartTime = "";
                if (subStartTime.endsWith("1")) {
                    replaceStartTime = subStartTime + "st";
                } else if (subStartTime.endsWith("2")) {
                    replaceStartTime = subStartTime + "nd";
                } else if (subStartTime.endsWith("3")) {
                    replaceStartTime = subStartTime + "rd";
                } else {
                    replaceStartTime = subStartTime + "th";
                }
                strTime = replaceStartTime + strTime.substring(2);
                return strTime;
            }
        }
    }

    private boolean isToday(long time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long timeInMillis = cal.getTimeInMillis() + ZoneUtil.getTestTime2(timeZone);
        return time >= timeInMillis && time < timeInMillis + TimeConstants.DAY;
    }

    private void refreshUI(OperationRecords.OperationRecord operationRecord, TextView tvMessage, ImageView ivLogState, TextView tvTime, View vPoint) {
        tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.c333333));
        @DrawableRes int imageResId = operationRecord.getDrawablePic();
        // TODO: 2021/3/29 有筛选并进行颜色更换
        ivLogState.setImageResource(imageResId);
        String message = operationRecord.getMessage();
        tvMessage.setText(message);
        if (message.contains("Alarm")) {
            tvTime.setTextColor(this.mContext.getColor(R.color.cFF556D));
            vPoint.setBackgroundResource(R.drawable.ic_circle_red_bg);
        } else {
            tvTime.setTextColor(this.mContext.getColor(R.color.c2C68FF));
            vPoint.setBackgroundResource(R.drawable.ic_circle_blue_bg);
        }
        tvTime.setText(ZoneUtil.getDate("+00:00", operationRecord.getOperationTime(), "HH:mm"));
    }
}
