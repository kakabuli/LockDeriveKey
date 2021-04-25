package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改无感开锁参数请求实体
 */
public class ChangeOpenLockParameterBeanReq implements Parcelable {


    /**
     * sn : KV51203710172
     * approachStatus : 1
     * approachTime : 12334345
     * longitude :
     * latitude :
     */

    private String sn;                  // 设备唯一编号
    private int approachStatus;         // 靠近开锁开关状态：1开 0关
    private int approachTime;           // 无感开门的时间，单位秒
    private String longitude;           // 经度
    private String latitude;            // 纬度

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getApproachStatus() {
        return approachStatus;
    }

    public void setApproachStatus(int approachStatus) {
        this.approachStatus = approachStatus;
    }

    public int getApproachTime() {
        return approachTime;
    }

    public void setApproachTime(int approachTime) {
        this.approachTime = approachTime;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeInt(this.approachStatus);
        dest.writeInt(this.approachTime);
        dest.writeString(this.longitude);
        dest.writeString(this.latitude);
    }

    public ChangeOpenLockParameterBeanReq() {
    }

    protected ChangeOpenLockParameterBeanReq(Parcel in) {
        this.sn = in.readString();
        this.approachStatus = in.readInt();
        this.approachTime = in.readInt();
        this.longitude = in.readString();
        this.latitude = in.readString();
    }

    public static final Parcelable.Creator<ChangeOpenLockParameterBeanReq> CREATOR = new Parcelable.Creator<ChangeOpenLockParameterBeanReq>() {
        @Override
        public ChangeOpenLockParameterBeanReq createFromParcel(Parcel source) {
            return new ChangeOpenLockParameterBeanReq(source);
        }

        @Override
        public ChangeOpenLockParameterBeanReq[] newArray(int size) {
            return new ChangeOpenLockParameterBeanReq[size];
        }
    };
}
