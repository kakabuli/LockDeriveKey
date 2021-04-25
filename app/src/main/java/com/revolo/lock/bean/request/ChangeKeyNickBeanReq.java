package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : Jack
 * time   : 2021/1/21
 * E-mail : wengmaowei@kaadas.com
 * desc   : 修改秘钥昵称请求实体
 */
public class ChangeKeyNickBeanReq implements Parcelable {


    /**
     * uid : 5c70d9493c554639ea93cc90
     * sn : GI132231004
     * pwdType : 1
     * num : 1
     * nickName : 密码1
     */

    private String uid;             // 管理员用户ID
    private String sn;              // 设备唯一编号
    private int pwdType;            // 密钥类型：1密码 2指纹密码 3卡片密码 4人脸
    private int num;                // 密钥编号
    private String nickName;        // 密钥昵称

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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
        dest.writeString(this.nickName);
    }

    public ChangeKeyNickBeanReq() {
    }

    protected ChangeKeyNickBeanReq(Parcel in) {
        this.uid = in.readString();
        this.sn = in.readString();
        this.pwdType = in.readInt();
        this.num = in.readInt();
        this.nickName = in.readString();
    }

    public static final Parcelable.Creator<ChangeKeyNickBeanReq> CREATOR = new Parcelable.Creator<ChangeKeyNickBeanReq>() {
        @Override
        public ChangeKeyNickBeanReq createFromParcel(Parcel source) {
            return new ChangeKeyNickBeanReq(source);
        }

        @Override
        public ChangeKeyNickBeanReq[] newArray(int size) {
            return new ChangeKeyNickBeanReq[size];
        }
    };
}
