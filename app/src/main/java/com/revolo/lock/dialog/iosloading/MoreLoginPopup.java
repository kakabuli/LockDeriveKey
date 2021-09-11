package com.revolo.lock.dialog.iosloading;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.revolo.lock.R;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.ScaleConfig;
import razerdp.util.animation.TranslationConfig;

public class MoreLoginPopup extends BasePopupWindow {

    private TextView mTvEmailLogin, mTvCancel;

    public MoreLoginPopup(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        View rootView = createPopupById(R.layout.popup_more_login);
        mTvEmailLogin = rootView.findViewById(R.id.tvEmailLogin);
        mTvCancel = rootView.findViewById(R.id.tvCancel);
        return rootView;
    }

    public void setEmailLoginListener(View.OnClickListener onClickListener) {
        if(mTvEmailLogin != null) {
            mTvEmailLogin.setOnClickListener(onClickListener);
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
