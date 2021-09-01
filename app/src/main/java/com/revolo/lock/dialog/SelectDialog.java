package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : 两个选择
 */
public class SelectDialog extends Dialog {

    private TextView mTvContent, mTvConfirm, mTvCancel;
    private String mMsg, mConfirmText, mCancelText;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;
    private boolean isReturn = false;

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

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
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

    public void setCancelText(String text) {
        mCancelText = text;
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
        if (mCancelText != null && mTvCancel != null) {
            mTvCancel.setText(mCancelText);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        Timber.e("onkeyDown:" + isReturn);
        if (isReturn) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        Timber.e("onKeyUp:" + isReturn);
        if (isReturn) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
