package com.revolo.lock.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Entity
public class BleDeviceLocal {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "d_id")
    private int id;                                         // 自增长id

    @ColumnInfo(name = "d_user_id")
    private int userId;                                     // 用户id

    @ColumnInfo(name = "d_pwd1")
    private String pwd1;                                    // 密码1

    @ColumnInfo(name = "d_pwd2")
    private String pwd2;                                    // 密码2

    @ColumnInfo(name = "d_esn")
    private String esn;                                     // ESN

    @ColumnInfo(name = "d_mac")
    private String mac;                                     // 设备蓝牙MAC

    @ColumnInfo(name = "d_scan_result_json")
    private String scanResultJson;                          // 检索到的蓝牙设备结果数据

    @ColumnInfo(name = "d_type")
    private String type;                                    // 设备的型号

    @ColumnInfo(name = "d_wifi_ver")
    private String wifiVer;                                 // Wifi的版本号

    @ColumnInfo(name = "d_lock_ver")
    private String lockVer;                                 // 锁端的版本号

    @ColumnInfo(name = "d_door_sensor")
    private int doorSensor;                                 // 门磁的当前状态

    @ColumnInfo(name = "d_connected_wifi_name")
    private String connectedWifiName;                       // 连接上的wifi名称

    @ColumnInfo(name = "d_connected_type")
    private int connectedType;                              // 连接的类型，wifi还是ble

    @ColumnInfo(name = "d_lock_state")
    private int lockState;                                  // 锁的开关状态（还有私密模式,存在分享用户）

    @ColumnInfo(name = "d_lock_power")
    private int lockPower;                                  // 锁的剩余电量

    @ColumnInfo(name = "d_set_auto_lock_time")
    private int setAutoLockTime;                            // 设置自动上锁时间

    @ColumnInfo(name = "d_is_detection_lock", defaultValue = "false")
    private boolean isDetectionLock;                        // 是否检测门锁

    @ColumnInfo(name = "d_set_electric_fence_time")
    private int setElectricFenceTime;                       // 设置电子围栏时间

    @ColumnInfo(name = "d_set_electric_fence_sensitivity")
    private int setElectricFenceSensitivity;                // 设置电子围栏灵敏度

    @ColumnInfo(name = "d_is_open_electric_fence", defaultValue = "false")
    private boolean isOpenElectricFence;                    // 是否开启电子围栏

    @ColumnInfo(name = "d_is_open_door_sensor", defaultValue = "false")
    private boolean isOpenDoorSensor;                       // 是否开启门磁

    @ColumnInfo(name = "d_is_mute", defaultValue = "false")
    private boolean isMute;                                 // 是否开启静音

    @ColumnInfo(name = "d_is_do_not_disturb_mode", defaultValue = "false")
    private boolean isDoNotDisturbMode;                     // 是否开启勿扰模式


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPwd1() {
        return pwd1;
    }

    public void setPwd1(String pwd1) {
        this.pwd1 = pwd1;
    }

    public String getPwd2() {
        return pwd2;
    }

    public void setPwd2(String pwd2) {
        this.pwd2 = pwd2;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getScanResultJson() {
        return scanResultJson;
    }

    public void setScanResultJson(String scanResultJson) {
        this.scanResultJson = scanResultJson;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWifiVer() {
        return wifiVer;
    }

    public void setWifiVer(String wifiVer) {
        this.wifiVer = wifiVer;
    }

    public String getLockVer() {
        return lockVer;
    }

    public void setLockVer(String lockVer) {
        this.lockVer = lockVer;
    }

    public int getDoorSensor() {
        return doorSensor;
    }

    public void setDoorSensor(int doorSensor) {
        this.doorSensor = doorSensor;
    }

    public String getConnectedWifiName() {
        return connectedWifiName;
    }

    public void setConnectedWifiName(String connectedWifiName) {
        this.connectedWifiName = connectedWifiName;
    }

    public int getConnectedType() {
        return connectedType;
    }

    public void setConnectedType(int connectedType) {
        this.connectedType = connectedType;
    }

    public int getLockState() {
        return lockState;
    }

    public void setLockState(int lockState) {
        this.lockState = lockState;
    }

    public int getLockPower() {
        return lockPower;
    }

    public void setLockPower(int lockPower) {
        this.lockPower = lockPower;
    }

    public int getSetAutoLockTime() {
        return setAutoLockTime;
    }

    public void setSetAutoLockTime(int setAutoLockTime) {
        this.setAutoLockTime = setAutoLockTime;
    }

    public boolean isDetectionLock() {
        return isDetectionLock;
    }

    public void setDetectionLock(boolean detectionLock) {
        isDetectionLock = detectionLock;
    }

    public int getSetElectricFenceTime() {
        return setElectricFenceTime;
    }

    public void setSetElectricFenceTime(int setElectricFenceTime) {
        this.setElectricFenceTime = setElectricFenceTime;
    }

    public int getSetElectricFenceSensitivity() {
        return setElectricFenceSensitivity;
    }

    public void setSetElectricFenceSensitivity(int setElectricFenceSensitivity) {
        this.setElectricFenceSensitivity = setElectricFenceSensitivity;
    }

    public boolean isOpenElectricFence() {
        return isOpenElectricFence;
    }

    public void setOpenElectricFence(boolean openElectricFence) {
        isOpenElectricFence = openElectricFence;
    }

    public boolean isOpenDoorSensor() {
        return isOpenDoorSensor;
    }

    public void setOpenDoorSensor(boolean openDoorSensor) {
        isOpenDoorSensor = openDoorSensor;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public boolean isDoNotDisturbMode() {
        return isDoNotDisturbMode;
    }

    public void setDoNotDisturbMode(boolean doNotDisturbMode) {
        isDoNotDisturbMode = doNotDisturbMode;
    }
}
