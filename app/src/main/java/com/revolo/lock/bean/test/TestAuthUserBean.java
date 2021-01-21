package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/15
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试的用户数据
 */
public class TestAuthUserBean implements Parcelable {

    private String mailAddress;
    private List<TestDeviceBean> mDeviceBeans;

    public static class TestDeviceBean implements Parcelable {
        private String deviceName;
        private int state;          // 1 邀请中 2 过期 3 成功
        private int per;            // 1 family 2 guest 3 closed permission

        public TestDeviceBean(String deviceName, int state, int per) {
            this.deviceName = deviceName;
            this.state = state;
            this.per = per;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getPer() {
            return per;
        }

        public void setPer(int per) {
            this.per = per;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.deviceName);
            dest.writeInt(this.state);
            dest.writeInt(this.per);
        }

        protected TestDeviceBean(Parcel in) {
            this.deviceName = in.readString();
            this.state = in.readInt();
            this.per = in.readInt();
        }

        public static final Parcelable.Creator<TestDeviceBean> CREATOR = new Parcelable.Creator<TestDeviceBean>() {
            @Override
            public TestDeviceBean createFromParcel(Parcel source) {
                return new TestDeviceBean(source);
            }

            @Override
            public TestDeviceBean[] newArray(int size) {
                return new TestDeviceBean[size];
            }
        };
    }

    public TestAuthUserBean(String mailAddress, List<TestDeviceBean> deviceBeans) {
        this.mailAddress = mailAddress;
        mDeviceBeans = deviceBeans;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public List<TestDeviceBean> getDeviceBeans() {
        return mDeviceBeans;
    }

    public void setDeviceBeans(List<TestDeviceBean> deviceBeans) {
        mDeviceBeans = deviceBeans;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mailAddress);
        dest.writeTypedList(this.mDeviceBeans);
    }

    protected TestAuthUserBean(Parcel in) {
        this.mailAddress = in.readString();
        this.mDeviceBeans = in.createTypedArrayList(TestDeviceBean.CREATOR);
    }

    public static final Parcelable.Creator<TestAuthUserBean> CREATOR = new Parcelable.Creator<TestAuthUserBean>() {
        @Override
        public TestAuthUserBean createFromParcel(Parcel source) {
            return new TestAuthUserBean(source);
        }

        @Override
        public TestAuthUserBean[] newArray(int size) {
            return new TestAuthUserBean[size];
        }
    };
}
