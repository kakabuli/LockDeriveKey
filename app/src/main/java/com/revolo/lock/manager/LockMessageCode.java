package com.revolo.lock.manager;

/**
 * 定义消息code
 */
public class LockMessageCode {
    public static final int MSG_LOCK_MESSAGE_USER=0;//用户操作
    public static final int MSG_LOCK_MESSAGE_BLE=1;//蓝牙操作
    public static final int MSG_LOCK_MESSAGE_MQTT=2;//MQTT 操作
    public static final int MSG_LOCK_MESSAGE_REMOVE_DEVICE=120; //解绑设备

}
