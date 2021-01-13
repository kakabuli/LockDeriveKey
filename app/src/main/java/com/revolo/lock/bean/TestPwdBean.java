package com.revolo.lock.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试的密码数据
 */
public class TestPwdBean implements Parcelable {

    private String pwdName;
    private String pwdDetail;
    private int pwdState;       // 1: 可用  2: 不可用
    private String pwd;
    private String pwdCharacteristic;
    private String createDate;

    public TestPwdBean(String pwdName, String pwdDetail, int pwdState, String pwd, String pwdCharacteristic, String createDate) {
        this.pwdName = pwdName;
        this.pwdDetail = pwdDetail;
        this.pwdState = pwdState;
        this.pwd = pwd;
        this.pwdCharacteristic = pwdCharacteristic;
        this.createDate = createDate;
    }

    public String getPwdName() {
        return pwdName;
    }

    public void setPwdName(String pwdName) {
        this.pwdName = pwdName;
    }

    public String getPwdDetail() {
        return pwdDetail;
    }

    public void setPwdDetail(String pwdDetail) {
        this.pwdDetail = pwdDetail;
    }

    public int getPwdState() {
        return pwdState;
    }

    public void setPwdState(int pwdState) {
        this.pwdState = pwdState;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwdCharacteristic() {
        return pwdCharacteristic;
    }

    public void setPwdCharacteristic(String pwdCharacteristic) {
        this.pwdCharacteristic = pwdCharacteristic;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pwdName);
        dest.writeString(this.pwdDetail);
        dest.writeInt(this.pwdState);
        dest.writeString(this.pwd);
        dest.writeString(this.pwdCharacteristic);
        dest.writeString(this.createDate);
    }

    protected TestPwdBean(Parcel in) {
        this.pwdName = in.readString();
        this.pwdDetail = in.readString();
        this.pwdState = in.readInt();
        this.pwd = in.readString();
        this.pwdCharacteristic = in.readString();
        this.createDate = in.readString();
    }

    public static final Creator<TestPwdBean> CREATOR = new Creator<TestPwdBean>() {
        @Override
        public TestPwdBean createFromParcel(Parcel source) {
            return new TestPwdBean(source);
        }

        @Override
        public TestPwdBean[] newArray(int size) {
            return new TestPwdBean[size];
        }
    };
}
