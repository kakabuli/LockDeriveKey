package com.revolo.lock.ble;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自定义蓝牙设备监听
 */
public interface OnBleDeviceListener {

    void onConnected();
    void onDisconnected();
    void onReceivedValue(String uuid, byte[] value);
    void onWriteValue(String uuid, byte[] value, boolean success);

}
