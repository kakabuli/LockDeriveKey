package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : esn输入错误提示
 */
public class CloseWiFiDialog extends Dialog {

    private TextView mTvContent, mTvConfirm;
    private String mMsg, mConfirmText;
    private View.OnClickListener mOnClickListener;
    private CheckBox checkBox;

    public CloseWiFiDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_close_wifi);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
        checkBox = findViewById(R.id.checkbox);
        refreshView();
    }

    public void setMessage(String msg) {
        this.mMsg = msg;
    }

    public void setOnListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setConfirmText(String text) {
        mConfirmText = text;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    private void refreshView() {
        if (mTvContent != null && mMsg != null) {
            mTvContent.setText(mMsg);
        }
        if (mOnClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnClickListener);
        }
        if (!TextUtils.isEmpty(mConfirmText) && mTvConfirm != null) {
            mTvConfirm.setText(mConfirmText);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
