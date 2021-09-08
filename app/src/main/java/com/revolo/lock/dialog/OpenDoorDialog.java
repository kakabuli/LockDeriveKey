package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * 打开门磁配置
 */
public class OpenDoorDialog extends Dialog {

    private TextView mTvContent, mTvConfirm;
    private View.OnClickListener mOnClickListener;

    public OpenDoorDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_open_door_view);
        setCanceledOnTouchOutside(false);
        mTvContent = findViewById(R.id.tvContent);
        mTvConfirm = findViewById(R.id.tvConfirm);
    }

    public void setmOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
        if (null != mTvConfirm) {
            mTvConfirm.setOnClickListener(listener);
        }
    }

    @Override
    public void show() {
        super.show();
        if (null != mTvConfirm) {
            mTvConfirm.setOnClickListener(mOnClickListener);
        }
    }
}
