package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class UpdateDoorSensorStateBeanReq implements Parcelable {


    /**
     * sn : KV51203710172
     * magneticStatus : 1
     */

    private String sn;
    private int magneticStatus;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getMagneticStatus() {
        return magneticStatus;
    }

    public void setMagneticStatus(int magneticStatus) {
        this.magneticStatus = magneticStatus;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeInt(this.magneticStatus);
    }

    public UpdateDoorSensorStateBeanReq() {
    }

    protected UpdateDoorSensorStateBeanReq(Parcel in) {
        this.sn = in.readString();
        this.magneticStatus = in.readInt();
    }

    public static final Parcelable.Creator<UpdateDoorSensorStateBeanReq> CREATOR = new Parcelable.Creator<UpdateDoorSensorStateBeanReq>() {
        @Override
        public UpdateDoorSensorStateBeanReq createFromParcel(Parcel source) {
            return new UpdateDoorSensorStateBeanReq(source);
        }

        @Override
        public UpdateDoorSensorStateBeanReq[] newArray(int size) {
            return new UpdateDoorSensorStateBeanReq[size];
        }
    };
}
