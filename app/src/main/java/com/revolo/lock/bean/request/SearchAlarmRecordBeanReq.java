package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class SearchAlarmRecordBeanReq implements Parcelable {


    /**
     * wifiSN : WF132231004
     * page : 1
     */

    private String wifiSN;
    private int page;

    public String getWifiSN() {
        return wifiSN;
    }

    public void setWifiSN(String wifiSN) {
        this.wifiSN = wifiSN;
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
        dest.writeString(this.wifiSN);
        dest.writeInt(this.page);
    }

    public SearchAlarmRecordBeanReq() {
    }

    protected SearchAlarmRecordBeanReq(Parcel in) {
        this.wifiSN = in.readString();
        this.page = in.readInt();
    }

    public static final Parcelable.Creator<SearchAlarmRecordBeanReq> CREATOR = new Parcelable.Creator<SearchAlarmRecordBeanReq>() {
        @Override
        public SearchAlarmRecordBeanReq createFromParcel(Parcel source) {
            return new SearchAlarmRecordBeanReq(source);
        }

        @Override
        public SearchAlarmRecordBeanReq[] newArray(int size) {
            return new SearchAlarmRecordBeanReq[size];
        }
    };
}
