package com.revolo.lock.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.revolo.lock.Constant;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.dialog.MessageDialog;

import timber.log.Timber;

public class WifiStateReceiver extends BroadcastReceiver {

    private MessageDialog mMessageDialog;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            String state = info.getState().name();
            if (state.equals(NetworkInfo.State.CONNECTED.name())) {
                Timber.d("WiFi state 连接成功");
            } else if (state.equals(NetworkInfo.State.CONNECTING.name())) {
                Timber.d("WiFi state 连接中");
            } else if (state.equals(NetworkInfo.State.DISCONNECTED.name())) {
                Timber.d("WiFi state 断开连接");
                initDialog();
                if (mMessageDialog != null && !Constant.isShowDialog) {
                    Constant.isShowDialog = true;
                    mMessageDialog.show();
                }
            } else if (state.equals(NetworkInfo.State.DISCONNECTING.name())) {
                Timber.d("WiFi state DISCONNECTING");
            } else if (state.equals(NetworkInfo.State.SUSPENDED.name())) {
                Timber.d("WiFi state SUSPENDED");
            } else if (state.equals(NetworkInfo.State.UNKNOWN.name())) {
                Timber.d("WiFi state UNKNOWN");
            }
        }
    }

    private void initDialog() {
        Activity activity = LockAppManager.getAppManager().currentActivity();
        if (mMessageDialog == null) {
            mMessageDialog = new MessageDialog(activity);
        }
        mMessageDialog.setMessage(activity.getString(R.string.t_wifi_disconnected_tip));
        mMessageDialog.setOnListener(v -> {
            if (mMessageDialog != null) {
                mMessageDialog.dismiss();
                Constant.isShowDialog = false;
            }
        });
    }
}
