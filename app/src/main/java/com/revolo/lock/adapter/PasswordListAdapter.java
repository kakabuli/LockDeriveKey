package com.revolo.lock.adapter;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestPwdBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表
 */
public class PasswordListAdapter extends BaseQuickAdapter<TestPwdBean, BaseViewHolder> {
    public PasswordListAdapter(int layoutResId, @Nullable List<TestPwdBean> data) {
        super(layoutResId, data);
    }

    public PasswordListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TestPwdBean testPwdBean) {
        if(testPwdBean != null) {
            TextView tvPwdName = baseViewHolder.getView(R.id.tvPwdName);
            TextView tvDetail = baseViewHolder.getView(R.id.tvDetail);
            if(testPwdBean.getPwdState() == 1) {
                baseViewHolder.setImageResource(R.id.ivPwdState, R.drawable.ic_home_icon_password);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.c333333));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.c666666));
            } else {
                baseViewHolder.setImageResource(R.id.ivPwdState, R.drawable.ic_home_icon_password_overdue);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
            }

            baseViewHolder.setText(R.id.tvPwdName, testPwdBean.getPwdName());
            baseViewHolder.setText(R.id.tvDetail, testPwdBean.getPwdDetail());
        }
    }
}
