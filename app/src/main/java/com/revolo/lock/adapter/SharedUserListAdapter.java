package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;

import org.jetbrains.annotations.NotNull;


/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 分享用户列表
 */
public class SharedUserListAdapter extends BaseQuickAdapter<GetAllSharedUserFromLockBeanRsp.DataBean, BaseViewHolder> {

    public SharedUserListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetAllSharedUserFromLockBeanRsp.DataBean bean) {
        if (bean != null) {
            baseViewHolder.setText(R.id.tvUserName, TextUtils.isEmpty(bean.getNickName()) ? "" : bean.getNickName());
            TextView tvPermission = baseViewHolder.getView(R.id.tvPermission);
            TextView tvUnlockOnly = baseViewHolder.getView(R.id.tvUnlockOnly);
            ImageView ivMore = baseViewHolder.getView(R.id.ivMore);
            if (bean.getShareUserType() == 1) {
                tvPermission.setText(getContext().getString(R.string.permission_family));
                tvUnlockOnly.setText(R.string.unable_to_add_user_and_password);
            } else {
                tvUnlockOnly.setText(R.string.per_app_unlock_only);
                tvPermission.setText(getContext().getString(R.string.permission_guest));
            }
            if (bean.getShareUserType() == 1) {
                ivMore.setImageResource(R.drawable.ic_icon_wait);
                ivMore.setVisibility(View.VISIBLE);
            } else if (bean.getShareUserType() == 3 || bean.getShareUserType() == 4 || bean.getShareUserType() == 5) {
                ivMore.setImageResource(R.mipmap.ic_icon_share);
                ivMore.setVisibility(View.VISIBLE);
            } else {
                ivMore.setVisibility(View.VISIBLE);
            }
        }
    }
}
