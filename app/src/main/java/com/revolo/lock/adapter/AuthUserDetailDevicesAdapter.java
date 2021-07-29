package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 分享的用户的详情页面下的设备列表
 */
public class AuthUserDetailDevicesAdapter extends BaseQuickAdapter<GetDevicesFromUidAndSharedUidBeanRsp.DataBean, BaseViewHolder> {

    private OnReInviteListener onReInviteListener;

    private OnDeleteListener mOnDeleteListener;

    public AuthUserDetailDevicesAdapter(int layoutResId) {
        super(layoutResId);
    }

    public void setOnReInviteListener(OnReInviteListener onReInviteListener) {
        this.onReInviteListener = onReInviteListener;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean) {
        if (bean != null) {
            // TODO: 2021/3/14 应该是设备名字，需要修改
            baseViewHolder.setText(R.id.tvDeviceName, TextUtils.isEmpty(bean.getLockNickname()) ? bean.getDeviceSN() : bean.getLockNickname());
            switch (bean.getShareState()) {
                case "0":
                    baseViewHolder.setImageResource(R.id.ivMore, R.drawable.ic_icon_more);
                    break;
                case "1":
                    baseViewHolder.setImageResource(R.id.ivMore, R.mipmap.ic_icon_prohibit);
                    break;
                case "2":
                    baseViewHolder.setImageResource(R.id.ivMore, R.drawable.ic_icon_wait);
                    break;
                case "3":
                    baseViewHolder.setImageResource(R.id.ivMore, R.mipmap.ic_icon_share);
                    break;
            }
            if (bean.getShareUserType() == 1) {
                baseViewHolder.setText(R.id.tvDetail, R.string.unable_to_add_user_and_password);
            } else if (bean.getShareUserType() == 2) {
                baseViewHolder.setText(R.id.tvDetail, R.string.per_app_unlock_only);
            }
            initPer(baseViewHolder, bean);
            baseViewHolder.getView(R.id.ivMore).setOnClickListener(v -> {
                onReInviteListener.onReInviteListener(bean);
            });
            baseViewHolder.getView(R.id.tv_delete).setOnClickListener(v -> {
                mOnDeleteListener.onDeleteClickListener(bean);
            });
        }
    }

    private void initPer(@NotNull BaseViewHolder holder, GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean) {
        if (bean.getShareUserType() == 1) {
            holder.setText(R.id.tvPer, R.string.permission_family);
        } else if (bean.getShareUserType() == 2) {
            holder.setText(R.id.tvPer, R.string.permission_guest);
        } else {
            holder.setText(R.id.tvPer, R.string.permission_closed_permission);
        }
    }

    public interface OnReInviteListener {
        void onReInviteListener(GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean);
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {

        void onDeleteClickListener(GetDevicesFromUidAndSharedUidBeanRsp.DataBean dataBean);
    }
}
