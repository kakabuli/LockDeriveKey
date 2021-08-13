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
 * desc   : 锁解除绑定
 */
public class UnbindLockDialog extends Dialog {

    private TextView mTvConfirm, mTvCancel, hintTextView;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;
    private boolean showHintText=true;

    public UnbindLockDialog(@NonNull Context context) {
        super(context);
        showHintText=true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_unbind_lock);
        setCanceledOnTouchOutside(false);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mTvCancel = findViewById(R.id.tvCancel);
        hintTextView = findViewById(R.id.textView58);
        if (showHintText) {
            hintTextView.setText(getContext().getString(R.string.dialog_tip_after_unbinding_your_door_lock_can_be_binded_by_others));
        } else {
            hintTextView.setText(getContext().getString(R.string.dialog_tip_line_device_unbin_text));
        }
        refreshView();
    }

    /**
     * 设置提示文本
     *
     * @param type
     */
    public void setHintText(boolean type) {
        showHintText=type;
        if (null != hintTextView) {
            if (type) {
                hintTextView.setText(getContext().getString(R.string.dialog_tip_after_unbinding_your_door_lock_can_be_binded_by_others));
            } else {
                hintTextView.setText(getContext().getString(R.string.dialog_tip_line_device_unbin_text));
            }
        }
    }

    public void setOnConfirmListener(View.OnClickListener listener) {
        mOnConfirmClickListener = listener;
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        mOnCancelClickListener = listener;
    }

    private void refreshView() {
        if (mOnConfirmClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnConfirmClickListener);
        }
        if (mOnCancelClickListener != null && mTvCancel != null) {
            mTvCancel.setOnClickListener(mOnCancelClickListener);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

}
