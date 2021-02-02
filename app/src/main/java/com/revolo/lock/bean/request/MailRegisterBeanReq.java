package com.revolo.lock.bean.request;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author :
 * time   : 2021/2/2
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class MailRegisterBeanReq implements Parcelable {


    /**
     * name : chason666@163.com
     * password : ll123654
     * tokens : 123456
     */

    private String name;
    private String password;
    private String tokens;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.password);
        dest.writeString(this.tokens);
    }

    public MailRegisterBeanReq() {
    }

    protected MailRegisterBeanReq(Parcel in) {
        this.name = in.readString();
        this.password = in.readString();
        this.tokens = in.readString();
    }

    public static final Parcelable.Creator<MailRegisterBeanReq> CREATOR = new Parcelable.Creator<MailRegisterBeanReq>() {
        @Override
        public MailRegisterBeanReq createFromParcel(Parcel source) {
            return new MailRegisterBeanReq(source);
        }

        @Override
        public MailRegisterBeanReq[] newArray(int size) {
            return new MailRegisterBeanReq[size];
        }
    };
}
