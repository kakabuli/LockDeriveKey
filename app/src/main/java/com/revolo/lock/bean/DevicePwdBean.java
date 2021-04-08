package com.revolo.lock.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.revolo.lock.ble.BleCommandState;

/**
 * author :
 * time   : 2021/2/3
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */

public class DevicePwdBean implements Parcelable {

    private long deviceId;                         // 设备ID
    private long createTime;                      // 创建时间
    private int pwdNum;                           // 密码编号
    private long startTime;                       // 开始时间
    private long endTime;                         // 结束时间
    private int attribute;                        // 密码属性
    private byte weekly;                          // 周策略
    private String pwdName;                       // 密码名称
    private int pwdState;                         // 1: 可用  2: 不可用

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.deviceId);
        dest.writeLong(this.createTime);
        dest.writeInt(this.pwdNum);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeInt(this.attribute);
        dest.writeByte(this.weekly);
        dest.writeString(this.pwdName);
        dest.writeInt(this.pwdState);
    }

    public void readFromParcel(Parcel source) {
        this.deviceId = source.readLong();
        this.createTime = source.readLong();
        this.pwdNum = source.readInt();
        this.startTime = source.readLong();
        this.endTime = source.readLong();
        this.attribute = source.readInt();
        this.weekly = source.readByte();
        this.pwdName = source.readString();
        this.pwdState = source.readInt();
    }

    public DevicePwdBean() {
    }

    protected DevicePwdBean(Parcel in) {
        this.deviceId = in.readLong();
        this.createTime = in.readLong();
        this.pwdNum = in.readInt();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.attribute = in.readInt();
        this.weekly = in.readByte();
        this.pwdName = in.readString();
        this.pwdState = in.readInt();
    }

    public static final Parcelable.Creator<DevicePwdBean> CREATOR = new Parcelable.Creator<DevicePwdBean>() {
        @Override
        public DevicePwdBean createFromParcel(Parcel source) {
            return new DevicePwdBean(source);
        }

        @Override
        public DevicePwdBean[] newArray(int size) {
            return new DevicePwdBean[size];
        }
    };
}
