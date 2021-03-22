package com.revolo.lock.popup;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.revolo.lock.R;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.ScaleConfig;
import razerdp.util.animation.TranslationConfig;

/**
 * author : Jack
 * time   : 2021/3/22
 * E-mail : wengmaowei@kaadas.com
 * desc   : 图片选择框
 */
public class PicSelectPopup extends BasePopupWindow {

    private TextView mTvPicSelect, mTvCamera, mTvCancel;

    public PicSelectPopup(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        View rootView = createPopupById(R.layout.popup_pic_select);
        mTvPicSelect = rootView.findViewById(R.id.tvPicSelect);
        mTvCamera = rootView.findViewById(R.id.tvCamera);
        mTvCancel = rootView.findViewById(R.id.tvCancel);
        return rootView;
    }

    public void setPicSelectOnClickListener(View.OnClickListener onClickListener) {
        if(mTvPicSelect != null) {
            mTvPicSelect.setOnClickListener(onClickListener);
        }
    }

    public void setCameraOnClickListener(View.OnClickListener onClickListener) {
        if(mTvCamera != null) {
            mTvCamera.setOnClickListener(onClickListener);
        }
    }

    public void setCancelOnClickListener(View.OnClickListener onClickListener) {
        if(mTvCancel != null) {
            mTvCancel.setOnClickListener(onClickListener);
        }
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return AnimationHelper.asAnimation()
                .withTranslation(TranslationConfig.FROM_BOTTOM)
                .toShow();
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return AnimationHelper.asAnimation()
                .withScale(ScaleConfig.TOP_TO_BOTTOM)
                .toDismiss();
    }

}
