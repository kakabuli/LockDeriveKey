package com.revolo.lock.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.revolo.lock.Constant;
import com.revolo.lock.LockAppManager;
import com.revolo.lock.R;
import com.revolo.lock.dialog.MessageDialog;

import timber.log.Timber;

public class WifiStateReceiver extends BroadcastReceiver {

    private MessageDialog mMessageDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("android.net.wifi.RSSI_CHANGED")) {
                int i = obtainWifiInfo(context);
                if (i < 2) {
//                    initDialog();
                    Timber.i("*****************    WiFi 信号弱    *****************");
                }
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

    /**
     * wifi信号强度 0 - 5 个强度  0 最弱
     *
     * @param context
     * @return
     */
    private int obtainWifiInfo(Context context) {
        int strength = 0;
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo.getBSSID() != null) {
            strength = wifiManager.calculateSignalLevel(connectionInfo.getRssi(), 5);
        }
        return strength;
    }
}
