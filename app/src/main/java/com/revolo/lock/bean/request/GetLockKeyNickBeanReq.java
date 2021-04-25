package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetLockKeyNickBeanReq implements Parcelable {


    /**
     * uid : 5c70d9493c554639ea93cc90
     * sn : GI132231004
     * pwdType : 1
     * num : 1
     */

    private String uid;
    private String sn;
    private int pwdType;
    private int num;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getPwdType() {
        return pwdType;
    }

    public void setPwdType(int pwdType) {
        this.pwdType = pwdType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.sn);
        dest.writeInt(this.pwdType);
        dest.writeInt(this.num);
    }

    public GetLockKeyNickBeanReq() {
    }

    protected GetLockKeyNickBeanReq(Parcel in) {
        this.uid = in.readString();
        this.sn = in.readString();
        this.pwdType = in.readInt();
        this.num = in.readInt();
    }

    public static final Parcelable.Creator<GetLockKeyNickBeanReq> CREATOR = new Parcelable.Creator<GetLockKeyNickBeanReq>() {
        @Override
        public GetLockKeyNickBeanReq createFromParcel(Parcel source) {
            return new GetLockKeyNickBeanReq(source);
        }

        @Override
        public GetLockKeyNickBeanReq[] newArray(int size) {
            return new GetLockKeyNickBeanReq[size];
        }
    };
}
