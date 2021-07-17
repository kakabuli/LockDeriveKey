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
            TextView tvUnableAddUser = baseViewHolder.getView(R.id.tvUnableAddUser);
            TextView tvUnableAddPwd = baseViewHolder.getView(R.id.tvUnableAddPwd);
            TextView tvUnlockOnly = baseViewHolder.getView(R.id.tvUnlockOnly);
            ImageView ivState = baseViewHolder.getView(R.id.ivState);
            ImageView ivMore = baseViewHolder.getView(R.id.ivMore);
            if (bean.getShareUserType() == 1) {
                tvPermission.setText(getContext().getString(R.string.permission_family));
                tvUnableAddUser.setVisibility(View.VISIBLE);
                tvUnableAddPwd.setVisibility(View.VISIBLE);
                tvUnlockOnly.setVisibility(View.GONE);
            } else {
                tvPermission.setText(getContext().getString(R.string.permission_guest));
                tvUnableAddUser.setVisibility(View.GONE);
                tvUnableAddPwd.setVisibility(View.GONE);
                tvUnlockOnly.setVisibility(View.VISIBLE);
            }
            if (bean.getShareUserType() == 1) {
                ivMore.setVisibility(View.GONE);
                ivState.setImageResource(R.drawable.ic_icon_wait);
                ivState.setVisibility(View.VISIBLE);
            } else if (bean.getShareUserType() == 3 || bean.getShareUserType() == 4 || bean.getShareUserType() == 5) {
                ivMore.setVisibility(View.GONE);
                ivState.setImageResource(R.drawable.ic_icon_invalid);
                ivState.setVisibility(View.VISIBLE);
            } else {
                ivMore.setVisibility(View.VISIBLE);
                ivState.setVisibility(View.GONE);
            }
        }
    }
}
