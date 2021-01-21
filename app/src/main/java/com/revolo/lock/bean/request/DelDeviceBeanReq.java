package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class DelDeviceBeanReq implements Parcelable {


    /**
     * uid : 5c4fe492dc93897aa7d8600b
     * sn : BT05191410011
     */

    private String uid;
    private String sn;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.sn);
    }

    public DelDeviceBeanReq() {
    }

    protected DelDeviceBeanReq(Parcel in) {
        this.uid = in.readString();
        this.sn = in.readString();
    }

    public static final Parcelable.Creator<DelDeviceBeanReq> CREATOR = new Parcelable.Creator<DelDeviceBeanReq>() {
        @Override
        public DelDeviceBeanReq createFromParcel(Parcel source) {
            return new DelDeviceBeanReq(source);
        }

        @Override
        public DelDeviceBeanReq[] newArray(int size) {
            return new DelDeviceBeanReq[size];
        }
    };
}
