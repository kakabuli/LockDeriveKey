package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : 注销账号提示
 */
public class AccountCancellationDialog extends Dialog {

    private TextView mTvContent, mTvCancel, mTvCancellation;
    private View.OnClickListener mOnCancellationListener, mOnCancelListener;

    public AccountCancellationDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_account_cancellation);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvCancel = findViewById(R.id.tvCancel);
        mTvCancellation = findViewById(R.id.tvCancellation);
        refreshView();
    }

    public void setOnCancellationClickListener(View.OnClickListener listener) {
        mOnCancellationListener = listener;
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        mOnCancelListener = listener;
    }

    private void refreshView() {
        if (mOnCancelListener != null && mTvCancel != null) {
            mTvCancel.setOnClickListener(mOnCancelListener);
        }
        if (mOnCancellationListener != null && mTvCancellation != null) {
            mTvCancellation.setOnClickListener(mOnCancellationListener);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
