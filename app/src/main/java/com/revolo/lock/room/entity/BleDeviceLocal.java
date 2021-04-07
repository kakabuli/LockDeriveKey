package com.revolo.lock.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.revolo.lock.LocalState;
import com.revolo.lock.ble.BleCommandState;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Entity
public class BleDeviceLocal implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "d_id")
    private long id;                                                // 自增长id

    @ColumnInfo(name = "d_user_id")
    private long userId;                                            // 用户id

    @ColumnInfo(name = "d_pwd1")
    private String pwd1;                                            // 密码1

    @ColumnInfo(name = "d_pwd2")
    private String pwd2;                                            // 密码2

    @ColumnInfo(name = "d_esn")
    private String esn;                                             // ESN

    @ColumnInfo(name = "d_mac")
    private String mac;                                             // 设备蓝牙MAC

    @ColumnInfo(name = "d_name")
    private String name;                                            // 锁自定义的名字

    @ColumnInfo(name = "d_function_set")
    private String functionSet;                                     // 功能集

    @ColumnInfo(name = "d_create_time")
    private long createTime;                                        // 创建时间

    @ColumnInfo(name = "d_scan_result_json")
    private byte[] scanResultJson;                                  // 检索到的蓝牙设备结果数据

    @ColumnInfo(name = "d_type")
    private String type;                                            // 设备的型号

    @ColumnInfo(name = "d_wifi_ver")
    private String wifiVer;                                         // Wifi的版本号

    @ColumnInfo(name = "d_lock_ver")
    private String lockVer;                                         // 锁端的版本号

    @ColumnInfo(name = "d_door_sensor")
    private @LocalState.DoorSensor int doorSensor;                  // 门磁的当前状态

    @ColumnInfo(name = "d_connected_wifi_name")
    private String connectedWifiName;                               // 连接上的wifi名称

    @ColumnInfo(name = "d_connected_type", defaultValue = "2")
    private @LocalState.DeviceConnectType int connectedType;        // 连接的类型，wifi还是ble  1 wifi  2 ble, 默认为蓝牙

    @ColumnInfo(name = "d_lock_state")
    private @LocalState.LockState int lockState;                    // 锁的开关状态（还有私密模式,存在分享用户） 1 开  2 关  3 私密模式

    @ColumnInfo(name = "d_lock_power")
    private int lockPower;                                          // 锁的剩余电量

    @ColumnInfo(name = "d_set_auto_lock_time")
    private int setAutoLockTime;                                    // 设置自动上锁时间

    @ColumnInfo(name = "d_is_detection_lock", defaultValue = "false")
    private boolean isDetectionLock;                                // 是否检测门锁

    @ColumnInfo(name = "d_set_electric_fence_time")
    private int setElectricFenceTime;                               // 设置电子围栏时间

    @ColumnInfo(name = "d_set_electric_fence_sensitivity")
    private int setElectricFenceSensitivity;                        // 设置电子围栏敲门开门的灵敏度 1:灵敏度低  2：灵敏度中  3：灵敏度高

    @ColumnInfo(name = "d_is_auto_lock", defaultValue = "false")
    private boolean isAutoLock;                                     // 是否开启自动上锁

    @ColumnInfo(name = "d_is_open_electric_fence", defaultValue = "false")
    private boolean isOpenElectricFence;                            // 是否开启电子围栏

    @ColumnInfo(name = "d_is_open_door_sensor", defaultValue = "false")
    private boolean isOpenDoorSensor;                               // 是否开启门磁

    @ColumnInfo(name = "d_is_mute", defaultValue = "false")
    private boolean isMute;                                         // 是否开启静音

    @ColumnInfo(name = "d_is_do_not_disturb_mode", defaultValue = "false")
    private boolean isDoNotDisturbMode;                             // 是否开启勿扰模式

    @ColumnInfo(name = "d_is_duress")
    private boolean isDuress;                                       // 是否开启胁迫密码

    @ColumnInfo(name = "d_random_code")
    private String randomCode;                                      // randomCode 用于开关门

    @ColumnInfo(name = "d_latitude")
    private double latitude;                                          // 地理围栏纬度

    @ColumnInfo(name = "d_longitude")
    private double longitude;                                         // 地理围栏经度


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
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

    public byte[] getScanResultJson() {
        return scanResultJson;
    }

    public void setScanResultJson(byte[] scanResultJson) {
        this.scanResultJson = scanResultJson;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFunctionSet() {
        return functionSet;
    }

    public void setFunctionSet(String functionSet) {
        this.functionSet = functionSet;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public @LocalState.DoorSensor int getDoorSensor() {
        return doorSensor;
    }

    public void setDoorSensor(@LocalState.DoorSensor int doorSensor) {
        this.doorSensor = doorSensor;
    }

    public String getConnectedWifiName() {
        return connectedWifiName;
    }

    public void setConnectedWifiName(String connectedWifiName) {
        this.connectedWifiName = connectedWifiName;
    }

    public @LocalState.DeviceConnectType int getConnectedType() {
        return connectedType;
    }

    public void setConnectedType(@LocalState.DeviceConnectType int connectedType) {
        this.connectedType = connectedType;
    }

    public @LocalState.LockState int getLockState() {
        return lockState;
    }

    public void setLockState(@LocalState.LockState int lockState) {
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

    public void setSetElectricFenceSensitivity(@BleCommandState.KnockDoorSensitivity int setElectricFenceSensitivity) {
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

    public boolean isDuress() {
        return isDuress;
    }

    public void setDuress(boolean duress) {
        isDuress = duress;
    }

    public boolean isAutoLock() {
        return isAutoLock;
    }

    public void setAutoLock(boolean autoLock) {
        isAutoLock = autoLock;
    }

    public String getRandomCode() {
        return randomCode;
    }

    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.userId);
        dest.writeString(this.pwd1);
        dest.writeString(this.pwd2);
        dest.writeString(this.esn);
        dest.writeString(this.mac);
        dest.writeString(this.name);
        dest.writeString(this.functionSet);
        dest.writeLong(this.createTime);
        dest.writeByteArray(this.scanResultJson);
        dest.writeString(this.type);
        dest.writeString(this.wifiVer);
        dest.writeString(this.lockVer);
        dest.writeInt(this.doorSensor);
        dest.writeString(this.connectedWifiName);
        dest.writeInt(this.connectedType);
        dest.writeInt(this.lockState);
        dest.writeInt(this.lockPower);
        dest.writeInt(this.setAutoLockTime);
        dest.writeByte(this.isDetectionLock ? (byte) 1 : (byte) 0);
        dest.writeInt(this.setElectricFenceTime);
        dest.writeInt(this.setElectricFenceSensitivity);
        dest.writeByte(this.isAutoLock ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isOpenElectricFence ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isOpenDoorSensor ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMute ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isDoNotDisturbMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isDuress ? (byte) 1 : (byte) 0);
        dest.writeString(this.randomCode);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readLong();
        this.userId = source.readLong();
        this.pwd1 = source.readString();
        this.pwd2 = source.readString();
        this.esn = source.readString();
        this.mac = source.readString();
        this.name = source.readString();
        this.functionSet = source.readString();
        this.createTime = source.readLong();
        this.scanResultJson = source.createByteArray();
        this.type = source.readString();
        this.wifiVer = source.readString();
        this.lockVer = source.readString();
        this.doorSensor = source.readInt();
        this.connectedWifiName = source.readString();
        this.connectedType = source.readInt();
        this.lockState = source.readInt();
        this.lockPower = source.readInt();
        this.setAutoLockTime = source.readInt();
        this.isDetectionLock = source.readByte() != 0;
        this.setElectricFenceTime = source.readInt();
        this.setElectricFenceSensitivity = source.readInt();
        this.isAutoLock = source.readByte() != 0;
        this.isOpenElectricFence = source.readByte() != 0;
        this.isOpenDoorSensor = source.readByte() != 0;
        this.isMute = source.readByte() != 0;
        this.isDoNotDisturbMode = source.readByte() != 0;
        this.isDuress = source.readByte() != 0;
        this.randomCode = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
    }

    public BleDeviceLocal() {
    }

    protected BleDeviceLocal(Parcel in) {
        this.id = in.readLong();
        this.userId = in.readLong();
        this.pwd1 = in.readString();
        this.pwd2 = in.readString();
        this.esn = in.readString();
        this.mac = in.readString();
        this.name = in.readString();
        this.functionSet = in.readString();
        this.createTime = in.readLong();
        this.scanResultJson = in.createByteArray();
        this.type = in.readString();
        this.wifiVer = in.readString();
        this.lockVer = in.readString();
        this.doorSensor = in.readInt();
        this.connectedWifiName = in.readString();
        this.connectedType = in.readInt();
        this.lockState = in.readInt();
        this.lockPower = in.readInt();
        this.setAutoLockTime = in.readInt();
        this.isDetectionLock = in.readByte() != 0;
        this.setElectricFenceTime = in.readInt();
        this.setElectricFenceSensitivity = in.readInt();
        this.isAutoLock = in.readByte() != 0;
        this.isOpenElectricFence = in.readByte() != 0;
        this.isOpenDoorSensor = in.readByte() != 0;
        this.isMute = in.readByte() != 0;
        this.isDoNotDisturbMode = in.readByte() != 0;
        this.isDuress = in.readByte() != 0;
        this.randomCode = in.readString();
        this.latitude = in.readLong();
        this.longitude = in.readLong();
    }

    public static final Creator<BleDeviceLocal> CREATOR = new Creator<BleDeviceLocal>() {
        @Override
        public BleDeviceLocal createFromParcel(Parcel source) {
            return new BleDeviceLocal(source);
        }

        @Override
        public BleDeviceLocal[] newArray(int size) {
            return new BleDeviceLocal[size];
        }
    };
}
