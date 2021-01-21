package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ChangeBleVerBeanReq implements Parcelable {


    /**
     * sn : BT05191410011
     * user_id : 5c4fe492dc93897aa7d8600b
     * bleVersionType : 1
     */

    private String sn;
    private String user_id;
    private String bleVersionType;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBleVersionType() {
        return bleVersionType;
    }

    public void setBleVersionType(String bleVersionType) {
        this.bleVersionType = bleVersionType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.user_id);
        dest.writeString(this.bleVersionType);
    }

    public ChangeBleVerBeanReq() {
    }

    protected ChangeBleVerBeanReq(Parcel in) {
        this.sn = in.readString();
        this.user_id = in.readString();
        this.bleVersionType = in.readString();
    }

    public static final Parcelable.Creator<ChangeBleVerBeanReq> CREATOR = new Parcelable.Creator<ChangeBleVerBeanReq>() {
        @Override
        public ChangeBleVerBeanReq createFromParcel(Parcel source) {
            return new ChangeBleVerBeanReq(source);
        }

        @Override
        public ChangeBleVerBeanReq[] newArray(int size) {
            return new ChangeBleVerBeanReq[size];
        }
    };
}
