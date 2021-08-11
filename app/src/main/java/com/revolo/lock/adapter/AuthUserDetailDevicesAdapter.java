package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 分享的用户的详情页面下的设备列表
 */
public class AuthUserDetailDevicesAdapter extends BaseMultiItemQuickAdapter<GetDevicesFromUidAndSharedUidBeanRsp.DataBean, BaseViewHolder> {

    private OnReInviteListener onReInviteListener;

    private OnDeleteListener mOnDeleteListener;

    public AuthUserDetailDevicesAdapter(List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> dataBeans) {
        super(dataBeans);
        addItemType(0, R.layout.item_user_devices_rv);
        addItemType(1, R.layout.item_user_devices_type_2);
    }

    public void setOnReInviteListener(OnReInviteListener onReInviteListener) {
        this.onReInviteListener = onReInviteListener;
    }

    @Override
    public int getItemViewType(int position) {

        return super.getItemViewType(position);
    }

    @Override
    public void setList(@Nullable Collection<? extends GetDevicesFromUidAndSharedUidBeanRsp.DataBean> list) {
        super.setList(list);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean) {
        if (bean != null) {
            if (baseViewHolder.getItemViewType() == 0) {
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
                baseViewHolder.getView(R.id.ivMore).setOnClickListener(v -> {
                    onReInviteListener.onReInviteListener(bean);
                });
                baseViewHolder.getView(R.id.tv_delete).setOnClickListener(v -> {
                    mOnDeleteListener.onDeleteClickListener(bean);
                });
            } else if (baseViewHolder.getItemViewType() == 1) {
                baseViewHolder.setText(R.id.tvDeviceName, TextUtils.isEmpty(bean.getLockNickname()) ? "" : bean.getLockNickname());
            }
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
