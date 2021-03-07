package com.revolo.lock.ble;

import org.jetbrains.annotations.NotNull;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自定义蓝牙设备监听
 */
public interface OnBleDeviceListener {

    void onConnected(@NotNull String mac);
    void onDisconnected(@NotNull String mac);
    void onReceivedValue(@NotNull String mac, String uuid, byte[] value);
    void onWriteValue(@NotNull String mac, String uuid, byte[] value, boolean success);
    void onAuthSuc(@NotNull String mac);

}
