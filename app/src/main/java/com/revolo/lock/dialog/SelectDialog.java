package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : 两个选择
 */
public class SelectDialog extends Dialog {

    private TextView mTvContent, mTvConfirm, mTvCancel;
    private String mMsg, mConfirmText;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;

    public SelectDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mTvCancel = findViewById(R.id.tvCancel);
        refreshView();
    }

    public TextView getCancel() {
        return mTvCancel;
    }

    public void setMessage(String msg) {
        this.mMsg = msg;
    }

    public void setConfirmText(String text) {
        mConfirmText = text;
    }

    public void setOnConfirmListener(View.OnClickListener listener) {
        mOnConfirmClickListener = listener;
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        mOnCancelClickListener = listener;
    }

    private void refreshView() {
        if (mTvContent != null && mMsg != null) {
            mTvContent.setText(mMsg);
        }
        if (mOnConfirmClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnConfirmClickListener);
        }
        if (mOnCancelClickListener != null && mTvCancel != null) {
            mTvCancel.setOnClickListener(mOnCancelClickListener);
        }
        if (mConfirmText != null && mTvConfirm != null) {
            mTvConfirm.setText(mConfirmText);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
