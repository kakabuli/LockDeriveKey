package com.revolo.lock.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/22
 * E-mail : wengmaowei@kaadas.com
 * desc   : Wifi列表
 */
public class WifiSnListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public WifiSnListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, String s) {
        if(s != null) {
            holder.setText(R.id.tvWifiSn, s);
        }
    }
}
