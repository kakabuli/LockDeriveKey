package com.revolo.lock.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.TestUserManagementBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 首页的分享用户列表
 */
public class UserListAdapter extends BaseQuickAdapter<TestUserManagementBean, BaseViewHolder> {
    public UserListAdapter(int layoutResId, @Nullable List<TestUserManagementBean> data) {
        super(layoutResId, data);
    }

    public UserListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TestUserManagementBean testUserManagementBean) {
        if(testUserManagementBean != null) {
            baseViewHolder.setText(R.id.tvName, testUserManagementBean.getUserName());
            baseViewHolder.setText(R.id.tvPermission,
                    testUserManagementBean.getPermission()==1?
                            getContext().getString(R.string.family)
                            :getContext().getString(R.string.guest));
        }
    }
}
