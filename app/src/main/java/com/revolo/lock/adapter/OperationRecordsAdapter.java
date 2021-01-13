package com.revolo.lock.adapter;

import androidx.recyclerview.widget.RecyclerView;

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
 * desc   : 操作记录汇总列表
 */
public class OperationRecordsAdapter extends BaseQuickAdapter<TestOperationRecords, BaseViewHolder> {

    public OperationRecordsAdapter(int layoutResId, @Nullable List<TestOperationRecords> data) {
        super(layoutResId, data);
    }

    public OperationRecordsAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TestOperationRecords testOperationRecords) {
        if(testOperationRecords != null) {
            long time = testOperationRecords.getTitleOperationTime();
            if(TimeUtils.isToday(time)) {
                baseViewHolder.setText(R.id.tvTimeTitle, "Today");
            } else {
                // 减掉一天的时间
                if(TimeUtils.isToday(time+86400000)) {
                    baseViewHolder.setText(R.id.tvTimeTitle, "Yesterday");
                } else {
                    baseViewHolder.setText(R.id.tvTimeTitle, TimeUtils.millis2String(time, "MMM dd yyyy"));
                }
            }
            RecyclerView rvRecords = baseViewHolder.getView(R.id.rvRecords);
            AutoMeasureLinearLayoutManager linearLayoutManager =  new AutoMeasureLinearLayoutManager(getContext());
//            linearLayoutManager.setAutoMeasureEnabled(false);
//            rvRecords.setHasFixedSize(false);
            rvRecords.setLayoutManager(linearLayoutManager);
            rvRecords.setAdapter(new OperationRecordAdapter(R.layout.item_operation_record_rv, testOperationRecords.getOperationRecords()));
        }
    }
}
