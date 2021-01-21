package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetPwd1BeanReq implements Parcelable {


    /**
     * SN : GI132231004
     */

    private String SN;

    public String getSN() {
        return SN;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.SN);
    }

    public GetPwd1BeanReq() {
    }

    protected GetPwd1BeanReq(Parcel in) {
        this.SN = in.readString();
    }

    public static final Parcelable.Creator<GetPwd1BeanReq> CREATOR = new Parcelable.Creator<GetPwd1BeanReq>() {
        @Override
        public GetPwd1BeanReq createFromParcel(Parcel source) {
            return new GetPwd1BeanReq(source);
        }

        @Override
        public GetPwd1BeanReq[] newArray(int size) {
            return new GetPwd1BeanReq[size];
        }
    };
}
