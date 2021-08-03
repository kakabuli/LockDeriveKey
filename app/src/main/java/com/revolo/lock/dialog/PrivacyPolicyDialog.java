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
public class PrivacyPolicyDialog extends Dialog {

    private TextView mTvContent, mTvConfirm, mTvCancel;
    private View.OnClickListener mOnConfirmClickListener, mOnCancelClickListener;

    public PrivacyPolicyDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_privacy_policy);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mTvCancel = findViewById(R.id.tvCancel);
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

        String string = getContext().getString(R.string.dialog_privacy_policy);
        SpannableString spannableString = new SpannableString(string);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getContext().getColor(R.color.c2C68FF));
        spannableString.setSpan(colorSpan, 27, 42, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(underlineSpan, 27, 42, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {

            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getContext(), TermActivity.class);
                intent.putExtra(Constant.TERM_TYPE, Constant.TERM_TYPE_PRIVACY);
                getContext().startActivity(intent);
            }
        }, 27, 42, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mTvContent.setText(spannableString);
        mTvContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
