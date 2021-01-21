package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class ChangeFeaturesBeanReq implements Parcelable {


    /**
     * sn : BT01191910010
     * uid : 5c4fe492dc93897aa7d8600b
     * functionSet : 01
     */

    private String sn;
    private String uid;
    private String functionSet;

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

    public String getFunctionSet() {
        return functionSet;
    }

    public void setFunctionSet(String functionSet) {
        this.functionSet = functionSet;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.uid);
        dest.writeString(this.functionSet);
    }

    public ChangeFeaturesBeanReq() {
    }

    protected ChangeFeaturesBeanReq(Parcel in) {
        this.sn = in.readString();
        this.uid = in.readString();
        this.functionSet = in.readString();
    }

    public static final Parcelable.Creator<ChangeFeaturesBeanReq> CREATOR = new Parcelable.Creator<ChangeFeaturesBeanReq>() {
        @Override
        public ChangeFeaturesBeanReq createFromParcel(Parcel source) {
            return new ChangeFeaturesBeanReq(source);
        }

        @Override
        public ChangeFeaturesBeanReq[] newArray(int size) {
            return new ChangeFeaturesBeanReq[size];
        }
    };
}
