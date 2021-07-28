package com.revolo.lock.adapter;

import android.text.TextUtils;
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

    private OnReInviteListener onReInviteListener;

    public void setOnReInviteListener(OnReInviteListener onReInviteListener) {
        this.onReInviteListener = onReInviteListener;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetAllSharedUserFromLockBeanRsp.DataBean bean) {
        if (bean != null) {
            baseViewHolder.setText(R.id.tvUserName, (TextUtils.isEmpty(bean.getFirstName()) ? "" : bean.getFirstName()) + " " + (TextUtils.isEmpty(bean.getLastName()) ? "" : bean.getLastName()));
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

            switch (bean.getShareState()) {
                case "0":
                    ivMore.setImageResource(R.drawable.ic_icon_more);
                    break;
                case "1":
                    ivMore.setImageResource(R.mipmap.ic_icon_prohibit);
                    break;
                case "2":
                    ivMore.setImageResource(R.drawable.ic_icon_wait);
                    break;
                case "3":
                    ivMore.setImageResource(R.mipmap.ic_icon_share);
                    break;
            }

            baseViewHolder.getView(R.id.ivMore).setOnClickListener(v -> {
                onReInviteListener.onReInviteListener(bean);
            });
        }
    }

    public interface OnReInviteListener {
        void onReInviteListener(GetAllSharedUserFromLockBeanRsp.DataBean bean);
    }
}
