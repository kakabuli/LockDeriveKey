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
 * desc   : 门磁异常提示
 */
public class DoorMagneticAnomalyDialog extends Dialog {

    private TextView mTvConfirm;
    private View.OnClickListener mOnConfirmClickListener;

    public DoorMagneticAnomalyDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_door_magnetic_anomaly);
        setCanceledOnTouchOutside(false);
        mTvConfirm = findViewById(R.id.tvConfirm);
        refreshView();
    }

    public void setOnConfirmListener(View.OnClickListener listener) {
        mOnConfirmClickListener = listener;
    }

    private void refreshView() {
        if(mOnConfirmClickListener != null && mTvConfirm != null) {
            mTvConfirm.setOnClickListener(mOnConfirmClickListener);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }
}
