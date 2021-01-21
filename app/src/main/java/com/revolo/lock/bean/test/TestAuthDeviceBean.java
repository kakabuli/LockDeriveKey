package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试设备
 */
public class TestAuthDeviceBean implements Parcelable {

    private String deviceName;
    private String deviceSN;

    public TestAuthDeviceBean(String deviceName, String deviceSN) {
        this.deviceName = deviceName;
        this.deviceSN = deviceSN;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceSN);
    }

    protected TestAuthDeviceBean(Parcel in) {
        this.deviceName = in.readString();
        this.deviceSN = in.readString();
    }

    public static final Parcelable.Creator<TestAuthDeviceBean> CREATOR = new Parcelable.Creator<TestAuthDeviceBean>() {
        @Override
        public TestAuthDeviceBean createFromParcel(Parcel source) {
            return new TestAuthDeviceBean(source);
        }

        @Override
        public TestAuthDeviceBean[] newArray(int size) {
            return new TestAuthDeviceBean[size];
        }
    };
}
