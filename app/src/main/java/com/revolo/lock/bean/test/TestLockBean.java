package com.revolo.lock.bean.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/12
 * E-mail : wengmaowei@kaadas.com
 * desc   : 锁测试数据
 */
public class TestLockBean implements Parcelable {

    private String name;          // 锁名字
    private int doorState;        // 门的状态 1: 开门, 2: 关门
    private int internetState;    // 网络状态 1: Wifi, 2: 蓝牙
    private int modeState;        // 权限状态 1: 全模式 2: 隐私模式

    public TestLockBean(String name, int doorState, int internetState, int modeState) {
        this.name = name;
        this.doorState = doorState;
        this.internetState = internetState;
        this.modeState = modeState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDoorState() {
        return doorState;
    }

    public void setDoorState(int doorState) {
        this.doorState = doorState;
    }

    public int getInternetState() {
        return internetState;
    }

    public void setInternetState(int internetState) {
        this.internetState = internetState;
    }

    public int getModeState() {
        return modeState;
    }

    public void setModeState(int modeState) {
        this.modeState = modeState;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.doorState);
        dest.writeInt(this.internetState);
        dest.writeInt(this.modeState);
    }

    protected TestLockBean(Parcel in) {
        this.name = in.readString();
        this.doorState = in.readInt();
        this.internetState = in.readInt();
        this.modeState = in.readInt();
    }

    public static final Parcelable.Creator<TestLockBean> CREATOR = new Parcelable.Creator<TestLockBean>() {
        @Override
        public TestLockBean createFromParcel(Parcel source) {
            return new TestLockBean(source);
        }

        @Override
        public TestLockBean[] newArray(int size) {
            return new TestLockBean[size];
        }
    };
}
