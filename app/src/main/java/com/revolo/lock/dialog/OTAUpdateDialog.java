package com.revolo.lock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.R;

/**
 * author :
 * time   : 2021/1/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OTAUpdateDialog extends Dialog {

    private static final int MSG_DIALOG_SHOW_OUT_TIME = 0xf01;

    private Context mContext;

    public OTAUpdateDialog(Context context) {
        super(context);
        mContext = context;
    }

    public OTAUpdateDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public static class Builder {

        private Context context;
        private String message;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置提示信息
         *
         * @param message
         * @return
         */

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public OTAUpdateDialog create() {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_ota_update, null);
            OTAUpdateDialog loadingDialog = new OTAUpdateDialog(context, R.style.IosDialogStyle);
            TextView msgText = view.findViewById(R.id.tipTextView);
            msgText.setText(message);
            loadingDialog.setContentView(view);
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            return loadingDialog;
        }
    }

    @Override
    public void show() {
        super.show();
        mHandler.sendEmptyMessageDelayed(MSG_DIALOG_SHOW_OUT_TIME, 5 * 60 * 1000);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeMessages(MSG_DIALOG_SHOW_OUT_TIME);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_DIALOG_SHOW_OUT_TIME) {
                if (OTAUpdateDialog.this.isShowing()) {
                    dismiss();
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(mContext.getString(R.string.tip_content_ota_update_failed));
                }
            }
        }
    };
}
