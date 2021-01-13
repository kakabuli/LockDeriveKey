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

    public TestPwdBean(String pwdName, String pwdDetail, int pwdState) {
        this.pwdName = pwdName;
        this.pwdDetail = pwdDetail;
        this.pwdState = pwdState;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pwdName);
        dest.writeString(this.pwdDetail);
        dest.writeInt(this.pwdState);
    }

    protected TestPwdBean(Parcel in) {
        this.pwdName = in.readString();
        this.pwdDetail = in.readString();
        this.pwdState = in.readInt();
    }

    public static final Parcelable.Creator<TestPwdBean> CREATOR = new Parcelable.Creator<TestPwdBean>() {
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
