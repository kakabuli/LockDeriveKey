package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/2/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class GetCodeBeanReq implements Parcelable {


    /**
     * mail : 294381926@qq.com
     * world : 2
     */

    private String mail;                  // 邮箱地址
    private int world;                    // 国际版标识：1国内版 2国际版（null默认为国内版）

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mail);
        dest.writeInt(this.world);
    }

    public GetCodeBeanReq() {
    }

    protected GetCodeBeanReq(Parcel in) {
        this.mail = in.readString();
        this.world = in.readInt();
    }

    public static final Parcelable.Creator<GetCodeBeanReq> CREATOR = new Parcelable.Creator<GetCodeBeanReq>() {
        @Override
        public GetCodeBeanReq createFromParcel(Parcel source) {
            return new GetCodeBeanReq(source);
        }

        @Override
        public GetCodeBeanReq[] newArray(int size) {
            return new GetCodeBeanReq[size];
        }
    };
}
