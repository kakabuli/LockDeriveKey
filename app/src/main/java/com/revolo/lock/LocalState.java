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

    @IntDef(value = {DEVICE_CONNECT_TYPE_WIFI, DEVICE_CONNECT_TYPE_BLE})
    public @interface DeviceConnectType{}
    public static final int DEVICE_CONNECT_TYPE_WIFI = 1;
    public static final int DEVICE_CONNECT_TYPE_BLE = 2;

    @IntDef(value = {LOCK_STATE_OPEN, LOCK_STATE_CLOSE, LOCK_STATE_PRIVATE})
    public @interface LockState{}
    public static final int LOCK_STATE_OPEN = 1;
    public static final int LOCK_STATE_CLOSE = 2;
    public static final int LOCK_STATE_PRIVATE = 3;             // 私密模式，不允许做任何操作

    @IntDef(value = {VOLUME_STATE_OPEN, VOLUME_STATE_MUTE})
    public @interface VolumeState{}
    public static final int VOLUME_STATE_OPEN = 1;
    public static final int VOLUME_STATE_MUTE = 0;

    @IntDef(value = {DURESS_STATE_OPEN, DURESS_STATE_CLOSE})
    public @interface DuressState{}
    public static final int DURESS_STATE_OPEN = 0x01;
    public static final int DURESS_STATE_CLOSE = 0x00;

    @IntDef(value = {AUTO_STATE_OPEN, AUTO_STATE_CLOSE})
    public @interface AutoState{}
    public static final int AUTO_STATE_OPEN = 0x00;
    public static final int AUTO_STATE_CLOSE = 0x01;

    @IntDef(value = {DOOR_STATE_OPEN, DOOR_STATE_CLOSE})
    public @interface DoorState{}
    public static final int DOOR_STATE_OPEN = 1;
    public static final int DOOR_STATE_CLOSE = 0;

    @IntDef(value = {DOOR_SENSOR_OPEN, DOOR_SENSOR_CLOSE, DOOR_SENSOR_EXCEPTION, DOOR_SENSOR_INIT})
    public @interface DoorSensor{}
    public static final int DOOR_SENSOR_OPEN = 0x00;
    public static final int DOOR_SENSOR_CLOSE = 0x01;
    public static final int DOOR_SENSOR_EXCEPTION = 0x02;
    public static final int DOOR_SENSOR_INIT = -1;

}
