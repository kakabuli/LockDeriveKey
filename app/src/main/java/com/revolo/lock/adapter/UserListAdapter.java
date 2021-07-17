package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 首页的分享用户列表
 */
public class UserListAdapter extends BaseQuickAdapter<GetAllSharedUserFromAdminUserBeanRsp.DataBean, BaseViewHolder> {

    public UserListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetAllSharedUserFromAdminUserBeanRsp.DataBean bean) {
        if (bean != null) {
            baseViewHolder.setText(R.id.tvName, (TextUtils.isEmpty(bean.getFirstName()) ? "" : bean.getFirstName()) + (TextUtils.isEmpty(bean.getLastName()) ? "" : bean.getLastName()));
            baseViewHolder.setText(R.id.tvPermission, TextUtils.isEmpty(bean.getDeviceCount()) ? "0" : bean.getDeviceCount() + " devices");

            RequestOptions requestOptions = RequestOptions.circleCropTransform()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        //缓存
                    .skipMemoryCache(false)
                    .error(R.drawable.home_user_authorization_user);
            Glide.with(getContext())
                    .load(TextUtils.isEmpty(bean.getAvatarPath()) ? "" : bean.getAvatarPath())
                    .apply(requestOptions)
                    .into((ImageView) baseViewHolder.getView(R.id.ivAvatar));
        }
    }
}
