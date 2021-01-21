package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ChangeDeviceNameBeanReq implements Parcelable {


    /**
     * sn : GI132231004
     * uid : 5c70ac053c554639ea93cc85
     * lockNickName : 我的门锁
     */

    private String sn;
    private String uid;
    private String lockNickName;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLockNickName() {
        return lockNickName;
    }

    public void setLockNickName(String lockNickName) {
        this.lockNickName = lockNickName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.uid);
        dest.writeString(this.lockNickName);
    }

    public ChangeDeviceNameBeanReq() {
    }

    protected ChangeDeviceNameBeanReq(Parcel in) {
        this.sn = in.readString();
        this.uid = in.readString();
        this.lockNickName = in.readString();
    }

    public static final Parcelable.Creator<ChangeDeviceNameBeanReq> CREATOR = new Parcelable.Creator<ChangeDeviceNameBeanReq>() {
        @Override
        public ChangeDeviceNameBeanReq createFromParcel(Parcel source) {
            return new ChangeDeviceNameBeanReq(source);
        }

        @Override
        public ChangeDeviceNameBeanReq[] newArray(int size) {
            return new ChangeDeviceNameBeanReq[size];
        }
    };
}
