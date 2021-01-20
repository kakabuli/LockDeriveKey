package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author : Jack
 * time   : 2021/1/20
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码失败提示
 */
public class AddPwdFailDialog extends Dialog {
    public AddPwdFailDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_pwd_fail);
        setCanceledOnTouchOutside(false);
        TextView tvConfirm = findViewById(R.id.tvConfirm);
        tvConfirm.setOnClickListener(v -> cancel());
    }

}
