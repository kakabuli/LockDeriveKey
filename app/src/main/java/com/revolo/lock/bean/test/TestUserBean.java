package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/19
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试用户数据
 */
public class TestUserBean implements Parcelable {

    private String userName;
    private String email;

    public TestUserBean(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeString(this.email);
    }

    protected TestUserBean(Parcel in) {
        this.userName = in.readString();
        this.email = in.readString();
    }

    public static final Parcelable.Creator<TestUserBean> CREATOR = new Parcelable.Creator<TestUserBean>() {
        @Override
        public TestUserBean createFromParcel(Parcel source) {
            return new TestUserBean(source);
        }

        @Override
        public TestUserBean[] newArray(int size) {
            return new TestUserBean[size];
        }
    };
}
