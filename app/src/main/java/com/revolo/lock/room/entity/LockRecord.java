package com.revolo.lock.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

/**
 * author :
 * time   : 2021/2/25
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
@Entity(indices = {@Index(value = {"lr_device_id"})}, primaryKeys = {"lr_device_id", "lr_create_time"})
public class LockRecord {

    @ColumnInfo(name = "lr_event_type")
    private int eventType;

    @ColumnInfo(name = "lr_event_source")
    private int eventSource;

    @ColumnInfo(name = "lr_event_code")
    private int eventCode;

    @ColumnInfo(name = "lr_user_id")
    private int userId;

    @ColumnInfo(name = "lr_app_id")
    private int appId;

    @ColumnInfo(name = "lr_device_id")
    private long deviceId;

    @ColumnInfo(name = "lr_create_time")
    private long createTime;

    @ColumnInfo(name = "lr_last_name")
    private String lastName;

    @ColumnInfo(name = "lr_pwd_nick_name")
    private String pwdNickname;

    public String getPwdNickname() {
        return pwdNickname;
    }

    public void setPwdNickname(String pwdNickname) {
        this.pwdNickname = pwdNickname;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getEventSource() {
        return eventSource;
    }

    public void setEventSource(int eventSource) {
        this.eventSource = eventSource;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContentStr() {
        return eventType +
                "" + eventSource +
                "" + eventCode +
                "" + userId +
                "" + appId +
                "" + deviceId +
                "" + createTime +
                "" + lastName +
                "" + pwdNickname;
    }
}
