package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 查询密钥列表请求实体
 */
public class SearchKeyListBeanReq implements Parcelable {


    /**
     * uid : 5c70d9493c554639ea93cc90
     * sn : GI132231004
     * pwdType : 1
     */

    private String uid;       // 管理员用户ID
    private String sn;        // 设备唯一编号
    private int pwdType;      // 密钥类型：0所有密码 1密码 2指纹密码 3卡片密码 4人脸

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.sn);
        dest.writeInt(this.pwdType);
    }

    public SearchKeyListBeanReq() {
    }

    protected SearchKeyListBeanReq(Parcel in) {
        this.uid = in.readString();
        this.sn = in.readString();
        this.pwdType = in.readInt();
    }

    public static final Parcelable.Creator<SearchKeyListBeanReq> CREATOR = new Parcelable.Creator<SearchKeyListBeanReq>() {
        @Override
        public SearchKeyListBeanReq createFromParcel(Parcel source) {
            return new SearchKeyListBeanReq(source);
        }

        @Override
        public SearchKeyListBeanReq[] newArray(int size) {
            return new SearchKeyListBeanReq[size];
        }
    };
}
