package com.revolo.lock.adapter;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestMessageBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 信息列表
 */
public class MessageListAdapter extends BaseQuickAdapter<TestMessageBean, BaseViewHolder> {
    public MessageListAdapter(int layoutResId, @Nullable List<TestMessageBean> data) {
        super(layoutResId, data);
    }

    public MessageListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, TestMessageBean testMessageBean) {
        if(testMessageBean != null) {
            holder.setText(R.id.tvMessageTitle, testMessageBean.getTitle());
            holder.setText(R.id.tvTime, TimeUtils.millis2String(testMessageBean.getCreateTime(), "yyyy.MM.dd"));
        }
    }
}
