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
 * desc   : 操作记录蓝牙模式
 */
public class OperationBleDialog extends Dialog {

    private TextView mTvContent, mTvConfirm;
    private String mMsg, mConfirmText;
    private View.OnClickListener mOnClickListener;

    public OperationBleDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_operation_ble);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
        refreshView();
    }

    public void setMessage(String msg) {
        this.mMsg = msg;
    }

    public void setOnListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    private void refreshView() {
        if(mTvContent != null && mMsg != null) {
            mTvContent.setText(mMsg);
        }
        if(mOnClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
