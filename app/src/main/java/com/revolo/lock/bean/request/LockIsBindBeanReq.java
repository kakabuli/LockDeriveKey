package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class LockIsBindBeanReq implements Parcelable {


    /**
     * deviceSN : BT01201410001
     * user_id : 5c4fe492dc93897aa7d8600b
     */

    private String deviceSN;
    private String user_id;

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceSN);
        dest.writeString(this.user_id);
    }

    public LockIsBindBeanReq() {
    }

    protected LockIsBindBeanReq(Parcel in) {
        this.deviceSN = in.readString();
        this.user_id = in.readString();
    }

    public static final Parcelable.Creator<LockIsBindBeanReq> CREATOR = new Parcelable.Creator<LockIsBindBeanReq>() {
        @Override
        public LockIsBindBeanReq createFromParcel(Parcel source) {
            return new LockIsBindBeanReq(source);
        }

        @Override
        public LockIsBindBeanReq[] newArray(int size) {
            return new LockIsBindBeanReq[size];
        }
    };
}
