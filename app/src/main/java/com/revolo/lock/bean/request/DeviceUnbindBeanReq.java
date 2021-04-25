package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 设备解绑请求实体
 */
public class DeviceUnbindBeanReq implements Parcelable {


    /**
     * wifiSN : WF132231004
     * uid : 5c4fe492dc93897aa7d8600b
     */

    private String wifiSN;         // wifi模块ESN
    private String uid;            // 用户ID

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.wifiSN);
        dest.writeString(this.uid);
    }

    public DeviceUnbindBeanReq() {
    }

    protected DeviceUnbindBeanReq(Parcel in) {
        this.wifiSN = in.readString();
        this.uid = in.readString();
    }

    public static final Parcelable.Creator<DeviceUnbindBeanReq> CREATOR = new Parcelable.Creator<DeviceUnbindBeanReq>() {
        @Override
        public DeviceUnbindBeanReq createFromParcel(Parcel source) {
            return new DeviceUnbindBeanReq(source);
        }

        @Override
        public DeviceUnbindBeanReq[] newArray(int size) {
            return new DeviceUnbindBeanReq[size];
        }
    };
}
