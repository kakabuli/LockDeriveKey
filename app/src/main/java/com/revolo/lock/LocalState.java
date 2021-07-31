package com.revolo.lock;

import androidx.annotation.IntDef;

/**
 * author :
 * time   : 2021/3/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class LocalState {

    private LocalState() {
    }

    @IntDef(value = {DEVICE_CONNECT_TYPE_WIFI, DEVICE_CONNECT_TYPE_BLE, DEVICE_CONNECT_TYPE_WIFI_BLE, DEVICE_CONNECT_TYPE_DIS})
    public @interface DeviceConnectType {
    }

    public static final int DEVICE_CONNECT_TYPE_WIFI = 1;//WiFi模式
    public static final int DEVICE_CONNECT_TYPE_BLE = 2;//ble存在连接
    public static final int DEVICE_CONNECT_TYPE_WIFI_BLE = 3;//WiFi和ble同时存在连接
    public static final int DEVICE_CONNECT_TYPE_DIS = 4;//掉线模式

    @IntDef(value = {DEVICE_STATE_UPDATE_MQTT_APP, DEVICE_STATE_UPDATE_MQTT_DEVICE,
            DEVICE_STATE_UPDATE_WIFI, DEVICE_STATE_UPDATE_DEVICE_BLUETOOTH_DIS, DEVICE_STATE_UPDATE_BLUETOOTH_OFF})
    public @interface DeviceStateUpdateType {
    }

    public static final int DEVICE_STATE_UPDATE_MQTT_APP = 1;//MQTT App断开
    public static final int DEVICE_STATE_UPDATE_MQTT_DEVICE = 2;//MQTT 设备断开
    public static final int DEVICE_STATE_UPDATE_WIFI = 3;//WIFI或者网络关闭
    public static final int DEVICE_STATE_UPDATE_DEVICE_BLUETOOTH_DIS = 4;//设备蓝牙断开
    public static final int DEVICE_STATE_UPDATE_BLUETOOTH_OFF = 5;//蓝牙关闭

    @IntDef(value = {LOCK_STATE_SENSOR_CLOSE, LOCK_STATE_OPEN, LOCK_STATE_CLOSE, LOCK_STATE_PRIVATE})
    public @interface LockState {
    }

    public static final int LOCK_STATE_SENSOR_CLOSE = 0; // 门磁状态关
    public static final int LOCK_STATE_OPEN = 1;
    public static final int LOCK_STATE_CLOSE = 2;
    public static final int LOCK_STATE_PRIVATE = 3;             // 私密模式，不允许做任何操作

    @IntDef(value = {VOLUME_STATE_OPEN, VOLUME_STATE_MUTE})
    public @interface VolumeState {
    }

    public static final int VOLUME_STATE_OPEN = 0; // 语音
    public static final int VOLUME_STATE_MUTE = 1; // 静音

    @IntDef(value = {DURESS_STATE_OPEN, DURESS_STATE_CLOSE})
    public @interface DuressState {
    }

    public static final int DURESS_STATE_OPEN = 0x01;
    public static final int DURESS_STATE_CLOSE = 0x00;

    @IntDef(value = {AUTO_STATE_OPEN, AUTO_STATE_CLOSE})
    public @interface AutoState {
    }

    public static final int AUTO_STATE_OPEN = 0x00;
    public static final int AUTO_STATE_CLOSE = 0x01;

    @IntDef(value = {DOOR_STATE_OPEN, DOOR_STATE_CLOSE})
    public @interface DoorState {
    }

    public static final int DOOR_STATE_OPEN = 1;
    public static final int DOOR_STATE_CLOSE = 0;

    @IntDef(value = {DOOR_SENSOR_OPEN, DOOR_SENSOR_CLOSE, DOOR_SENSOR_EXCEPTION, DOOR_SENSOR_INIT})
    public @interface DoorSensor {
    }

    public static final int DOOR_SENSOR_OPEN = 0x00;
    public static final int DOOR_SENSOR_CLOSE = 0x01;
    public static final int DOOR_SENSOR_EXCEPTION = 0x02;
    public static final int DOOR_SENSOR_INIT = -1;


    @IntDef(value = {CONNECT_STATE_MQTT, CONNECT_STATE_BIND_DEVICE, CONNECT_STATE_MQTT_CONFIG_DOOR, CONNECT_STATE_CHECK_BLE})
    public @interface ConnectState {
    }

    public static final int CONNECT_STATE_MQTT = 0;//mqtt 连接
    public static final int CONNECT_STATE_BIND_DEVICE = 1;// 绑定设备
    public static final int CONNECT_STATE_MQTT_CONFIG_DOOR = 3;//配置门磁、WiFi
    public static final int CONNECT_STATE_CHECK_BLE = 4;//离线模式下连接ble

}
