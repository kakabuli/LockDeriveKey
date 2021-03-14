package com.revolo.lock.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 分享的用户的详情页面下的设备列表
 */
public class AuthUserDetailDevicesAdapter extends BaseQuickAdapter<GetDevicesFromUidAndSharedUidBeanRsp.DataBean, BaseViewHolder> {
    public AuthUserDetailDevicesAdapter(int layoutResId, @Nullable List<GetDevicesFromUidAndSharedUidBeanRsp.DataBean> data) {
        super(layoutResId, data);
    }

    public AuthUserDetailDevicesAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean) {
        if(bean != null) {
            String name = bean.getUserNickname();
            // TODO: 2021/3/14 应该是设备名字，需要修改
            baseViewHolder.setText(R.id.tvDeviceName, TextUtils.isEmpty(name)?"":name);
            baseViewHolder.setVisible(R.id.ivState, true);
            baseViewHolder.setGone(R.id.ivMore, true);
            if(bean.getShareType() == 1) {
                // 等待
                baseViewHolder.setText(R.id.tvDetail, R.string.accepting);
                baseViewHolder.setImageResource(R.id.ivState, R.drawable.ic_icon_wait);
            } else if(bean.getShareType() == 3) {
                // 超时
                baseViewHolder.setText(R.id.tvDetail, R.string.accepting);
                baseViewHolder.setImageResource(R.id.ivState, R.drawable.ic_icon_invalid);
            } else {
                baseViewHolder.setGone(R.id.ivState, true);
                baseViewHolder.setVisible(R.id.ivMore, true);
                if(bean.getShareUserType() == 1) {
                    baseViewHolder.setText(R.id.tvDetail, R.string.unable_to_add_user_and_password);
                } else if(bean.getShareUserType() == 2) {
                    baseViewHolder.setText(R.id.tvDetail, R.string.per_app_unlock_only);
                } else {
                    // TODO: 2021/1/15 缺少对应的提示
                }
            }
            initPer(baseViewHolder, bean);
        }
    }

    private void initPer(@NotNull BaseViewHolder holder, GetDevicesFromUidAndSharedUidBeanRsp.DataBean bean) {
        if(bean.getShareUserType() == 1) {
            holder.setText(R.id.tvPer, R.string.permission_family);
        } else if(bean.getShareUserType() == 2) {
            holder.setText(R.id.tvPer, R.string.permission_guest);
        } else {
            holder.setText(R.id.tvPer, R.string.permission_closed_permission);
        }
    }

}
