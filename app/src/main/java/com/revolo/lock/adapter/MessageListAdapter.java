package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;
import com.revolo.lock.util.ZoneUtil;

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
    private OnAcceptingListener mOnAcceptingListener;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public MessageListAdapter(int layoutResId) {
        super(layoutResId);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SystemMessageListBeanRsp.DataBean dataBean) {
        if (dataBean != null) {
            if (!TextUtils.isEmpty(dataBean.getTimeZone())) {
                String timeZone = "GMT" + dataBean.getTimeZone();
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            }
            holder.setText(R.id.tvMessageTitle, TextUtils.isEmpty(dataBean.getAlertTitle()) ? "" : dataBean.getAlertTitle());
            holder.setText(R.id.tvTime, dataBean.getMsgType() == 6 ? simpleDateFormat.format(dataBean.getPushAt() * 1000) : ZoneUtil.getZeroTimeZoneDate(dataBean.getPushAt() * 1000));
            holder.setText(R.id.tv_message_answer, TextUtils.isEmpty(dataBean.getAlertBody()) ? "" : dataBean.getAlertBody());
            if (dataBean.getMsgType() == 6) {
                if (dataBean.getIsAgree() == 0 && dataBean.getIsShowAgreeShare() == 1) {
                    holder.setVisible(R.id.tvAccepting, true);
                } else {
                    holder.setGone(R.id.tvAccepting, true);
                }
            } else {
                holder.setGone(R.id.tvAccepting, true);
            }
            TextView textView = holder.getView(R.id.tv_delete);
            textView.setOnClickListener(v -> {
                if (mOnDeleteListener != null) {
                    mOnDeleteListener.onDeleteClickListener(dataBean);
                }
            });
            holder.getView(R.id.tvAccepting).setOnClickListener(v -> {
                if (mOnAcceptingListener != null) {
                    mOnAcceptingListener.onAcceptingListener(holder.getAdapterPosition(), dataBean);
                }
            });
        }
    }

    public void setOnAcceptingListener(OnAcceptingListener onAcceptingListener) {
        this.mOnAcceptingListener = onAcceptingListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {

        void onDeleteClickListener(SystemMessageListBeanRsp.DataBean dataBean);
    }

    public interface OnAcceptingListener {

        void onAcceptingListener(int position, SystemMessageListBeanRsp.DataBean dataBean);
    }
}
