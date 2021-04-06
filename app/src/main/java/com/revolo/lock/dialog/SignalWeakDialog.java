package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author :
 * time   : 2021/4/6
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SignalWeakDialog extends Dialog {

    private TextView mTvConfirm, mTvCancel;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_signal_weak);
        setCanceledOnTouchOutside(false);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mTvCancel = findViewById(R.id.tvCancel);
        refreshView();
    }

    public SignalWeakDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    public void setOnConfirmListener(View.OnClickListener listener) {
        mOnConfirmClickListener = listener;
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        mOnCancelClickListener = listener;
    }

    private void refreshView() {
        if(mOnConfirmClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnConfirmClickListener);
        }
        if(mOnCancelClickListener != null && mTvCancel != null) {
            mTvCancel.setOnClickListener(mOnCancelClickListener);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

}
