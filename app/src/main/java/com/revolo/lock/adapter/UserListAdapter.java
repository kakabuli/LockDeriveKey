package com.revolo.lock.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.test.TestUserManagementBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 首页的分享用户列表
 */
public class UserListAdapter extends BaseQuickAdapter<GetAllSharedUserFromAdminUserBeanRsp.DataBean, BaseViewHolder> {
    public UserListAdapter(int layoutResId, @Nullable List<GetAllSharedUserFromAdminUserBeanRsp.DataBean> data) {
        super(layoutResId, data);
    }

    public UserListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, GetAllSharedUserFromAdminUserBeanRsp.DataBean bean) {
        if(bean != null) {
            baseViewHolder.setText(R.id.tvName, bean.getUserNickname());
            baseViewHolder.setText(R.id.tvPermission,
                    bean.getShareUserType()==1?
                            getContext().getString(R.string.family)
                            :getContext().getString(R.string.guest));
        }
    }
}
