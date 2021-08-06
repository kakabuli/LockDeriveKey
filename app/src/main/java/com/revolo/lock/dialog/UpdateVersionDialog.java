package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.ui.sign.TermActivity;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : 两个选择
 */
public class UpdateVersionDialog extends Dialog {

    private TextView mTvContent, mTvConfirm, mTvCancel;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;
    private View mLine;
    private String forceFlag = "0";
    private String message = "";

    public UpdateVersionDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    public void setContent(String forceFlag, String message) {
        this.forceFlag = forceFlag;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_version);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mTvCancel = findViewById(R.id.tvCancel);
        mLine = findViewById(R.id.v_line);
        refreshView();
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

        mTvContent.setText(message);
        if (forceFlag.equals("1")) {
            mLine.setVisibility(View.GONE);
            mTvCancel.setVisibility(View.GONE);
        } else {
            mLine.setVisibility(View.VISIBLE);
            mTvCancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
