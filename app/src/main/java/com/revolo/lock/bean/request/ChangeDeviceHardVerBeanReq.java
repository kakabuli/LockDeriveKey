package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ChangeDeviceHardVerBeanReq implements Parcelable {


    /**
     * sn : KV51203710172
     * user_id : 5f5eff1294a83e85c41d4ca3
     * bleVersion : 1.0.2
     * peripheralId : 5c4fe492dc93897aa7dccccc
     */

    private String sn;
    private String user_id;
    private String bleVersion;
    private String peripheralId;

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

    public String getBleVersion() {
        return bleVersion;
    }

    public void setBleVersion(String bleVersion) {
        this.bleVersion = bleVersion;
    }

    public String getPeripheralId() {
        return peripheralId;
    }

    public void setPeripheralId(String peripheralId) {
        this.peripheralId = peripheralId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.user_id);
        dest.writeString(this.bleVersion);
        dest.writeString(this.peripheralId);
    }

    public ChangeDeviceHardVerBeanReq() {
    }

    protected ChangeDeviceHardVerBeanReq(Parcel in) {
        this.sn = in.readString();
        this.user_id = in.readString();
        this.bleVersion = in.readString();
        this.peripheralId = in.readString();
    }

    public static final Parcelable.Creator<ChangeDeviceHardVerBeanReq> CREATOR = new Parcelable.Creator<ChangeDeviceHardVerBeanReq>() {
        @Override
        public ChangeDeviceHardVerBeanReq createFromParcel(Parcel source) {
            return new ChangeDeviceHardVerBeanReq(source);
        }

        @Override
        public ChangeDeviceHardVerBeanReq[] newArray(int size) {
            return new ChangeDeviceHardVerBeanReq[size];
        }
    };
}
