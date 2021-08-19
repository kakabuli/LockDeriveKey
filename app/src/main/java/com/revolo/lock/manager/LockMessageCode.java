package com.revolo.lock.manager;

/**
 * 定义消息code
 */
public class LockMessageCode {
    public static final int MSG_LOCK_MESSAGE_USER = 0;//用户操作
    public static final int MSG_LOCK_MESSAGE_BLE = 1;//蓝牙操作
    public static final int MSG_LOCK_MESSAGE_MQTT = 2;//MQTT 操作
    public static final int MSG_LOCK_MESSAGE_CODE_SUCCESS = 200;//操作成功
    public static final int MSG_LOCK_MESSAGE_ADD_DEVICE = 119;//添加设备
    public static final int MSG_LOCK_MESSAGE_REMOVE_DEVICE = 120; //解绑设备
    public static final int MSG_LOCK_MESSAGE_ADD_DEVICE_SERVICE = 121;//将设备添加到服务器端
    public static final int MSG_LOCK_MESSAGE_SET_MAGNETIC = 122; // 设置门磁
    public static final int MSG_LOCK_MESSAGE_APP_ROACH_OPEN = 123;// 无感开门
    public static final int MSG_LOCK_MESSAGE_CLOSE_WIFI = 124;// 关闭wifi
     public static final int MSG_LOCK_MESSAGE_SET_LOCK = 125; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_CREATE_PWD = 126;// 创建密码
    public static final int MSG_LOCK_MESSAGE_ADD_PWD = 127;// 秘钥属性添加
    public static final int MSG_LOCK_MESSAGE_UPDATE_PWD = 128; // 秘钥属性修改
    public static final int MSG_LOCK_MESSAGE_REMOVE_PWD = 129;  // 秘钥属性删除
    public static final int MSG_LOCK_MESSAGE_GATEWAY_STATE = 130;  // 获取网关状态
    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTR = 131;   // 设置门锁属性
    public static final int MSG_LOCK_MESSAGE_WF_EVEN = 132; // 操作事件
    public static final int MSG_LOCK_MESSAGE_RECORD = 133; // 记录
    public static final int MSG_LOCK_MESSAGE_UPDATE_DEVICE_STATE = 134; // 更新当前设备的状态
    public static final int MSG_LOCK_MESSAGE_CLASE_DEVICE = 135;//清理蓝牙连接
    public static final int MSG_LOCK_MESSAGE_OPEN_PERMISSION = 136;//清理蓝牙连接

    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTO = 125; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTRAUTOTIME = 126; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTRDURES = 127; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTRSENSITIVITY = 128; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_SET_LOCK_ATTRVOLUME = 129; // 开关门指令
    public static final int MSG_LOCK_MESSAGE_UPDATE_BLEDEVICELOCAL=137;//刷新BleDeviceLocal


}
