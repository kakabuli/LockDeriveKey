package com.revolo.lock.dialog.iosloading;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.revolo.lock.R;

/**
 * author :
 * time   : 2021/1/29
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CustomerLoadingDialog extends Dialog {
    private static int MSG_DIALOG_SHOW_OUT_TIME = 254;

    public CustomerLoadingDialog(Context context) {
        super(context);
    }

    public CustomerLoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {

        private Context context;
        private String message;
        private boolean isShowMessage = true;
        private boolean isCancelable = false;
        private boolean isCancelOutside = false;


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

        /**
         * 设置是否显示提示信息
         *
         * @param isShowMessage
         * @return
         */
        public Builder setShowMessage(boolean isShowMessage) {
            this.isShowMessage = isShowMessage;
            return this;
        }

        /**
         * 设置是否可以按返回键取消
         *
         * @param isCancelable
         * @return
         */

        public Builder setCancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * 设置是否可以取消
         *
         * @param isCancelOutside
         * @return
         */
        public Builder setCancelOutside(boolean isCancelOutside) {
            this.isCancelOutside = isCancelOutside;
            return this;
        }

        public CustomerLoadingDialog create() {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_loading, null);
            CustomerLoadingDialog loadingDialog = new CustomerLoadingDialog(context, R.style.IosDialogStyle);
            TextView msgText = view.findViewById(R.id.tipTextView);
            if (isShowMessage) {
                msgText.setText(message);
            } else {
                msgText.setVisibility(View.GONE);
            }
            loadingDialog.setContentView(view);
            loadingDialog.setCancelable(isCancelable);
            loadingDialog.setCanceledOnTouchOutside(isCancelOutside);
            return loadingDialog;

        }


    }

    @Override
    public void show() {
        super.show();
        mHandler.sendEmptyMessageDelayed(MSG_DIALOG_SHOW_OUT_TIME, 10 * 1000);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeMessages(MSG_DIALOG_SHOW_OUT_TIME);
    }

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_DIALOG_SHOW_OUT_TIME) {
                if (CustomerLoadingDialog.this.isShowing()) {
                    dismiss();
                }
            }
        }
    };
}
