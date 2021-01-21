package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class CheckDoorSensorStateBeanReq implements Parcelable {


    /**
     * sn : KV51203710172
     */

    private String sn;

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
        dest.writeString(this.sn);
    }

    public CheckDoorSensorStateBeanReq() {
    }

    protected CheckDoorSensorStateBeanReq(Parcel in) {
        this.sn = in.readString();
    }

    public static final Parcelable.Creator<CheckDoorSensorStateBeanReq> CREATOR = new Parcelable.Creator<CheckDoorSensorStateBeanReq>() {
        @Override
        public CheckDoorSensorStateBeanReq createFromParcel(Parcel source) {
            return new CheckDoorSensorStateBeanReq(source);
        }

        @Override
        public CheckDoorSensorStateBeanReq[] newArray(int size) {
            return new CheckDoorSensorStateBeanReq[size];
        }
    };
}
