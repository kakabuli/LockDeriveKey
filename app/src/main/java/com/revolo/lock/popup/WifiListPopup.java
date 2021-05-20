package com.revolo.lock.popup;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.revolo.lock.R;
import com.revolo.lock.adapter.WifiSnListAdapter;

import java.util.List;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.ScaleConfig;
import razerdp.util.animation.TranslationConfig;

/**
 * author : Jack
 * time   : 2021/1/22
 * E-mail : wengmaowei@kaadas.com
 * desc   : wifi列表弹窗
 */
public class WifiListPopup extends BasePopupWindow {

    private WifiSnListAdapter mWifiSnListAdapter;

    public WifiListPopup(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        View rootView = createPopupById(R.layout.popup_wifi_list);
        RecyclerView rvWifiSnList = rootView.findViewById(R.id.rvWifiSnList);
        mWifiSnListAdapter = new WifiSnListAdapter(R.layout.item_popup_wifi_sn_list_rv);
        rvWifiSnList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWifiSnList.setAdapter(mWifiSnListAdapter);
        return rootView;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        if(mWifiSnListAdapter != null) {
            mWifiSnListAdapter.setOnItemClickListener(onItemClickListener);
        }
    }

    public void updateWifiList(List<String> wifiList) {
        mWifiSnListAdapter.setList(wifiList);
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return AnimationHelper.asAnimation()
                .withTranslation(TranslationConfig.FROM_TOP)
                .toShow();
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return AnimationHelper.asAnimation()
                .withScale(ScaleConfig.BOTTOM_TO_TOP)
                .toDismiss();
    }
}
