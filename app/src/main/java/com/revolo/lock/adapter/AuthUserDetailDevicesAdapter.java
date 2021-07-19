package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 分享的用户的详情页面下的设备列表
 */
public class AuthUserDetailDevicesAdapter extends BaseQuickAdapter<GetAllSharedUserFromLockBeanRsp.DataBean, BaseViewHolder> {

    private OnReInviteListener onReInviteListener;

    public AuthUserDetailDevicesAdapter(int layoutResId) {
        super(layoutResId);
    }

    public void setOnReInviteListener(OnReInviteListener onReInviteListener) {
        this.onReInviteListener = onReInviteListener;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetAllSharedUserFromLockBeanRsp.DataBean bean) {
        if (bean != null) {
            String name = bean.getLockNickname();
            // TODO: 2021/3/14 应该是设备名字，需要修改
            baseViewHolder.setText(R.id.tvDeviceName, TextUtils.isEmpty(name) ? "" : name);
            baseViewHolder.setVisible(R.id.ivState, true);
            baseViewHolder.setGone(R.id.ivMore, true);
            if (bean.getShareState() == 2) {
                // 等待
                baseViewHolder.setImageResource(R.id.ivState, R.drawable.ic_icon_wait);
            } else if (bean.getShareState() == 3) {
                // 超时
                baseViewHolder.setGone(R.id.ivState, true);
                baseViewHolder.setImageResource(R.id.ivState, R.mipmap.ic_icon_share);
            } else {
                baseViewHolder.setGone(R.id.ivState, true);
                baseViewHolder.setVisible(R.id.ivMore, true);
            }
            if (bean.getShareUserType() == 1) {
                baseViewHolder.setText(R.id.tvDetail, R.string.unable_to_add_user_and_password);
            } else if (bean.getShareUserType() == 2) {
                baseViewHolder.setText(R.id.tvDetail, R.string.per_app_unlock_only);
            }
            initPer(baseViewHolder, bean);
            baseViewHolder.getView(R.id.ivState).setOnClickListener(v -> {
                onReInviteListener.onReInviteListener(bean);
            });
        }
    }

    private void initPer(@NotNull BaseViewHolder holder, GetAllSharedUserFromLockBeanRsp.DataBean bean) {
        if (bean.getShareUserType() == 1) {
            holder.setText(R.id.tvPer, R.string.permission_family);
        } else if (bean.getShareUserType() == 2) {
            holder.setText(R.id.tvPer, R.string.permission_guest);
        } else {
            holder.setText(R.id.tvPer, R.string.permission_closed_permission);
        }
    }

    public interface OnReInviteListener {
        void onReInviteListener(GetAllSharedUserFromLockBeanRsp.DataBean bean);
    }
}
