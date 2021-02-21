package com.revolo.lock.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.revolo.lock.ble.BleCommandState;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Entity
public class DevicePwd {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dp_id")
    private int id;                               // 自增长id

    @ColumnInfo(name = "dp_device_id")
    private long deviceId;                         // 设备ID

    @ColumnInfo(name = "dp_create_time")
    private long createTime;                      // 创建时间

    @ColumnInfo(name = "dp_pwd_num")
    private int pwdNum;                           // 密码编号

    @ColumnInfo(name = "dp_start_time")
    private long startTime;                       // 开始时间

    @ColumnInfo(name = "dp_end_time")
    private long endTime;                         // 结束时间

    @ColumnInfo(name = "dp_attribute")
    private int attribute;                        // 密码属性

    @ColumnInfo(name = "dp_weekly")
    private byte weekly;                          // 周策略

    @ColumnInfo(name = "dp_pwd_name")
    private String pwdName;                       // 密码名称

    @ColumnInfo(name = "dp_pwd_state", defaultValue = "1")
    private int pwdState;                         // 1: 可用  2: 不可用

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getPwdNum() {
        return pwdNum;
    }

    public void setPwdNum(int pwdNum) {
        this.pwdNum = pwdNum;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(@BleCommandState.KeySetAttribute int attribute) {
        this.attribute = attribute;
    }

    public byte getWeekly() {
        return weekly;
    }

    public void setWeekly(byte weekly) {
        this.weekly = weekly;
    }

    public String getPwdName() {
        return pwdName;
    }

    public void setPwdName(String pwdName) {
        this.pwdName = pwdName;
    }

    public int getPwdState() {
        return pwdState;
    }

    public void setPwdState(int pwdState) {
        this.pwdState = pwdState;
    }
}
