package com.revolo.lock.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 测试的用户
 */
public class TestUserManagementBean implements Parcelable {

    private String userName;
    private int permission;    // 1 family 2 guest
    private int state;         // 1 邀请中 2 无效 3 成功

    public TestUserManagementBean(String userName, int permission, int state) {
        this.userName = userName;
        this.permission = permission;
        this.state = state;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeInt(this.permission);
        dest.writeInt(this.state);
    }

    protected TestUserManagementBean(Parcel in) {
        this.userName = in.readString();
        this.permission = in.readInt();
        this.state = in.readInt();
    }

    public static final Parcelable.Creator<TestUserManagementBean> CREATOR = new Parcelable.Creator<TestUserManagementBean>() {
        @Override
        public TestUserManagementBean createFromParcel(Parcel source) {
            return new TestUserManagementBean(source);
        }

        @Override
        public TestUserManagementBean[] newArray(int size) {
            return new TestUserManagementBean[size];
        }
    };
}
