package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 信息列表
 */
public class MessageListAdapter extends BaseQuickAdapter<SystemMessageListBeanRsp.DataBean, BaseViewHolder> {

    private OnDeleteListener mOnDeleteListener;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MessageListAdapter(int layoutResId) {
        super(layoutResId);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SystemMessageListBeanRsp.DataBean dataBean) {
        if (dataBean != null) {
            holder.setText(R.id.tvMessageTitle, TextUtils.isEmpty(dataBean.getAlertTitle()) ? "" : dataBean.getAlertTitle());
            holder.setText(R.id.tvTime, TextUtils.isEmpty(dataBean.getPushAt()) ? "" : dataBean.getPushAt());
            holder.setText(R.id.tv_message_answer, TextUtils.isEmpty(dataBean.getAlertBody()) ? "" : dataBean.getAlertBody());
            addChildClickViewIds(R.id.tvAccepting);
            // TODO 分享的消息类型 等服务器定
            holder.setVisible(R.id.tvAccepting, dataBean.getMsgType().equals("4"));
            TextView textView = holder.getView(R.id.tv_delete);
            textView.setOnClickListener(v -> {
                if (mOnDeleteListener != null) {
                    mOnDeleteListener.onDeleteClickListener(dataBean);
                }
            });
        }
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {

        void onDeleteClickListener(SystemMessageListBeanRsp.DataBean dataBean);
    }
}
