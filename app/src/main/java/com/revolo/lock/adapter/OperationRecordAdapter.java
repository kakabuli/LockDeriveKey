package com.revolo.lock.adapter;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.TestOperationRecords;

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
            baseViewHolder.setText(R.id.tvMessage, testOperationRecord.getMessage());
            baseViewHolder.setText(R.id.tvTime, TimeUtils.millis2String(testOperationRecord.getOperationTime(), "HH:mm"));
        }
    }
}
