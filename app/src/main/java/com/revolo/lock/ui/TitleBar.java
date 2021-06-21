package com.revolo.lock.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.revolo.lock.R;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   : custom title bar
 */
public class TitleBar {

    private final TextView mTvTitle;
    private final ImageView mIvLeft;
    private final ImageView mIvRight;
    private final TextView mTvNetError;

    public TitleBar(View rootView) {
        mTvTitle = rootView.findViewById(R.id.tvTitle);
        mIvLeft = rootView.findViewById(R.id.ivLeft);
        mIvRight = rootView.findViewById(R.id.ivRight);
        mTvNetError = rootView.findViewById(R.id.tvNetError);
    }

    public TextView getTvTitle() {
        return mTvTitle;
    }

    public ImageView getIvLeft() {
        return mIvLeft;
    }

    public ImageView getIvRight() {
        return mIvRight;
    }

    public TitleBar setTitle(String title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
            mTvTitle.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public TitleBar useCommonLeft(View.OnClickListener onClickListener) {
        if (mIvLeft != null) {
            mIvLeft.setVisibility(View.VISIBLE);
            mIvLeft.setOnClickListener(onClickListener);
        }
        return this;
    }

    public TitleBar setRight(Drawable drawable, View.OnClickListener onClickListener) {
        if (mIvRight != null) {
            mIvRight.setVisibility(View.VISIBLE);
            mIvRight.setImageDrawable(drawable);
            mIvRight.setOnClickListener(onClickListener);
        }
        return this;
    }

    public TitleBar setRight(@DrawableRes int drawableId, View.OnClickListener onClickListener) {
        if (mIvRight != null) {
            mIvRight.setVisibility(View.VISIBLE);
            mIvRight.setImageResource(drawableId);
            mIvRight.setOnClickListener(onClickListener);
        }
        return this;
    }

    public void setNetError(boolean isConnect) {
        mTvNetError.setVisibility(isConnect ? View.GONE : View.VISIBLE);
    }
}
