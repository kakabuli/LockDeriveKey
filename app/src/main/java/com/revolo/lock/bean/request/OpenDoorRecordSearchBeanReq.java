package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OpenDoorRecordSearchBeanReq implements Parcelable {


    /**
     * sn : WF132231004
     * page : 1
     */

    private String sn;
    private int page;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeInt(this.page);
    }

    public OpenDoorRecordSearchBeanReq() {
    }

    protected OpenDoorRecordSearchBeanReq(Parcel in) {
        this.sn = in.readString();
        this.page = in.readInt();
    }

    public static final Parcelable.Creator<OpenDoorRecordSearchBeanReq> CREATOR = new Parcelable.Creator<OpenDoorRecordSearchBeanReq>() {
        @Override
        public OpenDoorRecordSearchBeanReq createFromParcel(Parcel source) {
            return new OpenDoorRecordSearchBeanReq(source);
        }

        @Override
        public OpenDoorRecordSearchBeanReq[] newArray(int size) {
            return new OpenDoorRecordSearchBeanReq[size];
        }
    };
}
