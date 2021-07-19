package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateLocalBeanReq implements Parcelable {
    private String sn;//" "设备sn",
    private String longitude;//""经度",
    private String latitude;//" "纬度",
    private int elecFence;//" "电子围栏开关"
    private int elecFenceTime;//":,"title": "电子围栏时间"
    private int elecFenceSensitivity;//"电子围栏灵敏度
    private int elecFenceState;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getElecFence() {
        return elecFence;
    }

    public void setElecFence(int elecFence) {
        this.elecFence = elecFence;
    }

    public int getElecFenceTime() {
        return elecFenceTime;
    }

    public void setElecFenceTime(int elecFenceTime) {
        this.elecFenceTime = elecFenceTime;
    }

    public int getElecFenceSensitivity() {
        return elecFenceSensitivity;
    }

    public void setElecFenceSensitivity(int elecFenceSensitivity) {
        this.elecFenceSensitivity = elecFenceSensitivity;
    }

    public int isElecFenceState() {
        return elecFenceState;
    }

    public void setElecFenceState(int elecFenceState) {
        this.elecFenceState = elecFenceState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(longitude);//""经度",
        dest.writeString(latitude);//" "纬度",
        dest.writeInt(elecFence);//" "电子围栏开关"
        dest.writeInt(elecFenceTime);//":,"title": "电子围栏时间"
        dest.writeInt(elecFenceSensitivity);//"电子围栏灵敏度
        dest.writeInt(elecFenceState);

    }

    public UpdateLocalBeanReq() {
    }

    protected UpdateLocalBeanReq(Parcel in) {
        this.sn = in.readString();
        this.longitude=in.readString();//""经度",
        this.latitude=in.readString();//" "纬度",
        this.elecFence=in.readInt();//" "电子围栏开关"
        this.elecFenceTime=in.readInt();//":,"title": "电子围栏时间"
        this.elecFenceSensitivity=in.readInt();//"电子围栏灵敏度
        this.elecFenceState=in.readInt();

    }

    public static final Parcelable.Creator<UpdateLocalBeanReq> CREATOR = new Parcelable.Creator<UpdateLocalBeanReq>() {
        @Override
        public UpdateLocalBeanReq createFromParcel(Parcel source) {
            return new UpdateLocalBeanReq(source);
        }

        @Override
        public UpdateLocalBeanReq[] newArray(int size) {
            return new UpdateLocalBeanReq[size];
        }
    };
}

